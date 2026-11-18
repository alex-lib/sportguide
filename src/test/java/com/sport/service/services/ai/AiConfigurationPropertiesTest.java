package com.sport.service.services.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiConfigurationPropertiesTest {
    @Test
    void applicationYamlLoadsWithoutDuplicateKeysAndKeepsExistingAppSettings() throws Exception {
        var source = new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml")).getFirst();
        assertEquals("openai/gpt-oss-120b", source.getProperty("app.ai.primary.model"));
        assertEquals("openrouter/free", source.getProperty("app.ai.fallback.model"));
        assertEquals(true, source.getProperty("app.redis.enable"));
    }
}
