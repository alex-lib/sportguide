package com.sport.service.services.ai;

import com.sport.service.configurations.AiProviderProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AiProviderRouterTest {
    private final Clock clock = mock(Clock.class);
    @SuppressWarnings("unchecked")
    private final Function<String, String> openAi = mock(Function.class);
    private MockRestServiceServer server;
    private AiProviderRouter router;
    private final Instant now = Instant.parse("2026-11-18T10:00:00Z");

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(now);
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        router = new AiProviderRouter(builder, new AiProviderProperties(
                new AiProviderProperties.Provider("https://groq.test/v1", "groq-test-key", "openai/gpt-oss-120b"),
                new AiProviderProperties.Provider("https://router.test/v1", "router-test-key", "openrouter/free")),
                openAi, clock);
    }

    private void success(String host, String key, String model) {
        server.expect(requestTo("https://" + host + ".test/v1/chat/completions"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + key))
                .andExpect(jsonPath("$.model").value(model))
                .andExpect(jsonPath("$.messages[0].content").value("brief"))
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"training\"},\"finish_reason\":\"stop\"}]}", MediaType.APPLICATION_JSON));
    }

    private void limited(String host, String retryAfter) {
        var response = withStatus(HttpStatus.TOO_MANY_REQUESTS);
        if (retryAfter != null) response.header(HttpHeaders.RETRY_AFTER, retryAfter);
        server.expect(requestTo("https://" + host + ".test/v1/chat/completions")).andRespond(response);
    }

    @Test
    void startsWithGroq() {
        success("groq", "groq-test-key", "openai/gpt-oss-120b");
        assertEquals("training", router.generate("brief"));
        verifyNoInteractions(openAi);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"60", "Wed, 18 Nov 2026 10:01:00 GMT"})
    void skipsGroqDuringCooldownAndReturnsAtExpiry(String retryAfter) {
        limited("groq", retryAfter);
        success("router", "router-test-key", "openrouter/free");
        success("router", "router-test-key", "openrouter/free");
        success("groq", "groq-test-key", "openai/gpt-oss-120b");
        assertEquals("training", router.generate("brief"));
        when(clock.instant()).thenReturn(now.plusSeconds(59));
        assertEquals("training", router.generate("brief"));
        when(clock.instant()).thenReturn(now.plusSeconds(60));
        assertEquals("training", router.generate("brief"));
        verifyNoInteractions(openAi);
        server.verify();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid", "-3", "999999999999999999999999", "0", "Wed, 18 Nov 2026 09:59:00 GMT"})
    void missingInvalidOrExpiredRetryAfterDoesNotLatchFallback(String retryAfter) {
        limited("groq", retryAfter);
        success("router", "router-test-key", "openrouter/free");
        success("groq", "groq-test-key", "openai/gpt-oss-120b");
        assertEquals("training", router.generate("brief"));
        assertEquals("training", router.generate("brief"));
        server.verify();
    }

    @Test
    void bothRateLimitedUseExistingOpenAiAndKeepGroqCooldown() {
        limited("groq", "60");
        limited("router", null);
        success("router", "router-test-key", "openrouter/free");
        when(openAi.apply("brief")).thenReturn("openai training");
        assertEquals("openai training", router.generate("brief"));
        assertEquals("training", router.generate("brief"));
        verify(openAi).apply("brief");
        server.verify();
    }

    @Test
    void authenticationErrorDoesNotSpendOnFallback() {
        server.expect(requestTo("https://groq.test/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        assertThrows(HttpClientErrorException.Unauthorized.class, () -> router.generate("brief"));
        verifyNoInteractions(openAi);
        server.verify();
    }

    @Test
    void openRouterNon429DoesNotSpendOnOpenAi() {
        limited("groq", null);
        server.expect(requestTo("https://router.test/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));
        assertThrows(HttpClientErrorException.Forbidden.class, () -> router.generate("brief"));
        verifyNoInteractions(openAi);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"choices\":[{\"message\":{\"content\":\"\"}}]}",
            "{\"choices\":[{\"message\":{\"content\":\"partial\"},\"finish_reason\":\"length\"}]}"})
    void rejectsInvalidOrTruncatedAnswers(String response) {
        server.expect(requestTo("https://groq.test/v1/chat/completions"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        assertThrows(IllegalStateException.class, () -> router.generate("brief"));
        verifyNoInteractions(openAi);
        server.verify();
    }
}
