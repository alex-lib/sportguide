package com.sport.service.services.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.sport.service.configurations.AiProviderProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Slf4j
public class AiProviderRouter {
    private final RestClient primary;
    private final RestClient fallback;
    private final AiProviderProperties properties;
    private final Function<String, String> lastResort;
    private final Clock clock;
    private final AtomicReference<Instant> primaryRetryAt = new AtomicReference<>(Instant.MIN);

    public AiProviderRouter(RestClient.Builder builder, AiProviderProperties properties,
                            Function<String, String> lastResort, Clock clock) {
        this.properties = properties;
        this.primary = client(builder, properties.primary());
        this.fallback = client(builder, properties.fallback());
        this.lastResort = lastResort;
        this.clock = clock;
    }

    private RestClient client(RestClient.Builder builder, AiProviderProperties.Provider provider) {
        return builder.clone().baseUrl(provider.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + provider.apiKey()).build();
    }

    public String generate(String prompt) {
        if (!clock.instant().isBefore(primaryRetryAt.get())) {
            try {
                return complete(primary, properties.primary(), prompt, "groq");
            } catch (HttpClientErrorException.TooManyRequests e) {
                rememberRetryAfter(e.getResponseHeaders());
                log.info("Groq rate limit; routing request to OpenRouter");
            }
        }
        try {
            return complete(fallback, properties.fallback(), prompt, "openrouter");
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.info("OpenRouter rate limit; routing request to OpenAI");
            return lastResort.apply(prompt);
        }
    }

    private String complete(RestClient client, AiProviderProperties.Provider provider, String prompt, String name) {
        if (provider.apiKey() == null || provider.apiKey().isBlank()) {
            throw new IllegalStateException("Missing AI credentials for " + name);
        }
        JsonNode response = client.post().uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("model", provider.model(), "temperature", 0.2,
                        "messages", List.of(Map.of("role", "user", "content", prompt))))
                .retrieve().body(JsonNode.class);
        if (response == null || !response.path("choices").path(0).path("message").path("content").isTextual()) {
            throw new IllegalStateException("Invalid AI response from " + name);
        }
        JsonNode choice = response.path("choices").path(0);
        String answer = choice.path("message").path("content").asText();
        if (answer.isBlank() || "length".equals(choice.path("finish_reason").asText())) {
            throw new IllegalStateException("Empty or truncated AI response from " + name);
        }
        log.info("AI generation completed: provider={}, model={}", name, response.path("model").asText(provider.model()));
        return answer;
    }

    private void rememberRetryAfter(HttpHeaders headers) {
        String value = headers == null ? null : headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null || value.isBlank()) return;
        try {
            Instant retryAt;
            if (value.trim().matches("[0-9]+")) {
                retryAt = clock.instant().plusSeconds(Long.parseLong(value.trim()));
            } else {
                retryAt = ZonedDateTime.parse(value.trim(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
            }
            primaryRetryAt.accumulateAndGet(retryAt, (current, next) -> current.isAfter(next) ? current : next);
        } catch (RuntimeException e) {
            // An absent/invalid delay affects only this request, never the rest of the day.
            log.warn("Ignoring invalid Groq Retry-After header");
        }
    }
}
