package com.sport.service.services.ai;

import com.sport.service.entities.enums.subscriber.RoleType;
import com.sport.service.entities.AiTrainingQuota;
import com.sport.service.exceptions.AiQuotaExceededException;
import com.sport.service.repositories.AiTrainingQuotaRepository;
import com.sport.service.repositories.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AiTrainingQuotaService {
    private final AiTrainingQuotaRepository quotas;
    private final SubscriberRepository subscribers;
    private final Clock clock;

    @Transactional
    public Instant reserve(long userId) {
        var subscriber = subscribers.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        if (subscriber.getRole() == RoleType.ADMIN) return null;
        Instant now = clock.instant().truncatedTo(ChronoUnit.MILLIS);
        Instant availableAt = now.plus(Duration.ofDays(1));
        int claimed = quotas.reserve(userId, availableAt, now);
        if (claimed == 0) {
            Instant nextTime = quotas.findById(userId).map(AiTrainingQuota::getAvailableAt).orElse(availableAt);
            throw new AiQuotaExceededException(nextTime);
        }
        return availableAt;
    }

    @Transactional
    public void release(long userId, Instant reservation) {
        if (reservation == null) return;
        quotas.release(userId, reservation);
    }
}
