package com.sport.service.services.ai;

import com.sport.service.exceptions.AiQuotaExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TrainingGenerationServiceTest {
    private final AiAssistantService assistant = mock(AiAssistantService.class);
    private final AiTrainingQuotaService quota = mock(AiTrainingQuotaService.class);
    private final TrainingGenerationService service = new TrainingGenerationService(assistant, quota);
    private final Instant reservation = Instant.parse("2026-10-30T10:00:00Z");

    @Test
    void successfulGenerationConsumesQuotaForAuthenticatedUser() {
        when(quota.reserve(10L)).thenReturn(reservation);
        when(assistant.generateTrainingProgram("Бег")).thenReturn("Программа");
        assertEquals("Программа", service.generate(10L, "Бег"));
        verify(quota).reserve(10L);
        verify(quota, never()).release(anyLong(), any());
    }

    @Test
    void exhaustedQuotaNeverCallsAiProvider() {
        when(quota.reserve(10L)).thenThrow(new AiQuotaExceededException(reservation));
        assertThrows(AiQuotaExceededException.class, () -> service.generate(10L, "Бег"));
        verifyNoInteractions(assistant);
    }

    @Test
    void failureReleasesOnlyThisReservation() {
        when(quota.reserve(10L)).thenReturn(reservation);
        when(assistant.generateTrainingProgram("Бег")).thenThrow(new IllegalStateException("Unavailable"));
        assertThrows(IllegalStateException.class, () -> service.generate(10L, "Бег"));
        verify(quota).release(10L, reservation);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void emptyProviderAnswerDoesNotUseQuota(String answer) {
        when(quota.reserve(10L)).thenReturn(reservation);
        when(assistant.generateTrainingProgram("Бег")).thenReturn(answer);
        assertThrows(IllegalStateException.class, () -> service.generate(10L, "Бег"));
        verify(quota).release(10L, reservation);
    }

    @Test
    void invalidQuestionDoesNotReserveQuota() {
        assertThrows(IllegalArgumentException.class, () -> service.generate(10L, " "));
        assertThrows(IllegalArgumentException.class, () -> service.generate(10L, "a".repeat(1001)));
        verifyNoInteractions(quota, assistant);
    }
}
