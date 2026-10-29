package com.sport.service.services.ai;

import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.subscriber.RoleType;
import com.sport.service.exceptions.AiQuotaExceededException;
import com.sport.service.repositories.SubscriberRepository;
import org.junit.jupiter.api.Test;
import com.sport.service.repositories.AiTrainingQuotaRepository;
import com.sport.service.entities.AiTrainingQuota;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AiTrainingQuotaServiceTest {
    private final AiTrainingQuotaRepository repository = mock(AiTrainingQuotaRepository.class);
    private final SubscriberRepository subscribers = mock(SubscriberRepository.class);
    private final Instant now = Instant.parse("2026-10-29T10:00:00Z");
    private final Instant tomorrow = now.plusSeconds(86400);
    private final AiTrainingQuotaService quota = new AiTrainingQuotaService(repository, subscribers, Clock.fixed(now, ZoneOffset.UTC));

    @Test
    void adminCanGenerateRepeatedlyWithoutQuotaStorage() {
        when(subscribers.findById(10L)).thenReturn(Optional.of(Subscriber.builder().role(RoleType.ADMIN).build()));
        assertNull(quota.reserve(10L));
        assertNull(quota.reserve(10L));
        verifyNoInteractions(repository);
    }

    @Test
    void subscriberGets24HourReservation() {
        when(subscribers.findById(10L)).thenReturn(Optional.of(Subscriber.builder().role(RoleType.SUBSCRIBER).build()));
        when(repository.reserve(10L, tomorrow, now)).thenReturn(1);
        assertEquals(tomorrow, quota.reserve(10L));
    }

    @Test
    void blockedClaimReturnsExistingResetTime() {
        when(subscribers.findById(10L)).thenReturn(Optional.of(Subscriber.builder().role(RoleType.SUBSCRIBER).build()));
        when(repository.findById(10L)).thenReturn(Optional.of(new AiTrainingQuota(10L, tomorrow)));
        var error = assertThrows(AiQuotaExceededException.class, () -> quota.reserve(10L));
        assertEquals(tomorrow, error.getAvailableAt());
    }

    @Test
    void releaseUsesReservationTimestampToProtectLaterRequests() {
        quota.release(10L, tomorrow);
        verify(repository).release(10L, tomorrow);
        quota.release(10L, null);
        verifyNoMoreInteractions(repository);
    }
}
