package com.sport.service.services.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class AiAssistantService {
    private final ChatClient chatClient;
    private final String templateText;

    public AiAssistantService(
            ChatClient chatClient,
            @Value("classpath:prompts/ai/sport_assistant.st") Resource template
    ) {
        this.chatClient = chatClient;
        this.templateText = loadTemplate(template);
    }

    private String loadTemplate(Resource resource) {
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to load AI template", e);
            throw new IllegalStateException("Cannot load AI prompt template", e);
        }
    }

    public String generateTrainingProgram(String question) {
        try {
            return chatClient.prompt()
                    .user(u -> u.text(templateText).param("question", question))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Error generating training program: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate training program", e);
        }
    }
}