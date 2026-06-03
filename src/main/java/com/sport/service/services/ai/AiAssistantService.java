package com.sport.service.services.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import java.nio.charset.StandardCharsets;

@Service
public class AiAssistantService {
    private final ChatClient chatClient;
    private final String templateText;

    public AiAssistantService(ChatClient chatClient, @Value("classpath:prompts/ai/sport_assistant.st") Resource template) {
        this.chatClient = chatClient;
        this.templateText = loadTemplate(template);
    }

    private String loadTemplate(Resource resource) {
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load AI prompt template", e);
        }
    }

    public String generateTrainingProgram(String question) {
        return chatClient
                .prompt()
                .user(u -> u.text(templateText).param("question", question))
                .call()
                .content();
    }
}