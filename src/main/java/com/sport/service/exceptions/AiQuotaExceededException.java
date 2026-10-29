package com.sport.service.exceptions;

import lombok.Getter;

import java.time.Instant;

@Getter
public class AiQuotaExceededException extends RuntimeException {
    private final Instant availableAt;

    public AiQuotaExceededException(Instant availableAt) {
        super("ИИ-тренер доступен один раз в 24 часа. Попробуйте позже.");
        this.availableAt = availableAt;
    }
}
