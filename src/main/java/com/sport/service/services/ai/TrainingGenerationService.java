package com.sport.service.services.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingGenerationService {
    private final AiAssistantService assistant;
    private final AiTrainingQuotaService quota;

    public String generate(long userId, String question) {
        if (question == null || question.isBlank() || question.length() > 1000) {
            throw new IllegalArgumentException("Введите пожелания к тренировке: от 1 до 1000 символов.");
        }
        Instant reservation = quota.reserve(userId);
        try {
            String answer = assistant.generateTrainingProgram(question);
            if (answer == null || answer.isBlank()) throw new IllegalStateException("AI returned an empty answer");
            return answer;
        } catch (RuntimeException e) {
            try {
                quota.release(userId, reservation);
            } catch (RuntimeException releaseError) {
                log.error("Failed to release AI quota for user {}", userId, releaseError);
                e.addSuppressed(releaseError);
            }
            throw e;
        }
    }
}
