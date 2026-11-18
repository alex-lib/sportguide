package com.sport.service.services.ai;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class AiAssistantService {
    private final AiProviderRouter router;
    private final String templateText;

    public AiAssistantService(
            AiProviderRouter router,
            @Value("classpath:prompts/ai/sport_assistant.st") Resource template
    ) {
        this.router = router;
        this.templateText = loadTemplate(template);
    }

    private String loadTemplate(Resource resource) {
        try (var input = resource.getInputStream()) {
            return StreamUtils.copyToString(input, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to load AI template", e);
            throw new IllegalStateException("Cannot load AI prompt template", e);
        }
    }

    public String generateTrainingProgram(String question) {
        try {
            return formatAnswer(router.generate(templateText.replace("{question}", question)));
        } catch (Exception e) {
            log.error("Error generating training program ({})", e.getClass().getSimpleName());
            throw new RuntimeException("Failed to generate training program", e);
        }
    }

    static String formatAnswer(String answer) {
        if (answer == null || answer.isBlank()) throw new IllegalStateException("AI returned an empty answer");
        String safe = Jsoup.clean(answer, "", new Safelist().addTags("b", "i"),
                new Document.OutputSettings().prettyPrint(false));
        if (Jsoup.parse(safe).text().isBlank() || safe.length() > 4000) {
            throw new IllegalStateException("AI answer is empty or exceeds the Telegram limit");
        }
        return safe;
    }
}
