package com.sport.service.services.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiAssistantServiceTest {
    @Test
    void repairsUnbalancedHtmlAndRemovesUnsupportedTagsAndAttributes() {
        String result = AiAssistantService.formatAnswer("<b onclick='bad'>Разминка</i>\n<ul><li>Ходьба</li></ul>");
        assertEquals("<b>Разминка\nХодьба</b>", result);
    }

    @Test
    void preservesParagraphsAndSupportedFormatting() {
        String answer = "<b>Разминка</b>\nХодьба 5 минут\n\n<i>Отдых</i>";
        assertEquals(answer, AiAssistantService.formatAnswer(answer));
    }

    @Test
    void rejectsEmptyAndOversizedAnswersInsteadOfSendingUndeliverableMessage() {
        assertThrows(IllegalStateException.class, () -> AiAssistantService.formatAnswer(null));
        assertThrows(IllegalStateException.class, () -> AiAssistantService.formatAnswer("<b></b>"));
        assertThrows(IllegalStateException.class, () -> AiAssistantService.formatAnswer("a".repeat(4001)));
        assertEquals(4000, AiAssistantService.formatAnswer("a".repeat(4000)).length());
    }
}
