package com.sport.service.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.ai")
public record AiProviderProperties(Provider primary, Provider fallback) {
    public record Provider(String baseUrl, String apiKey, String model) {
    }
}
