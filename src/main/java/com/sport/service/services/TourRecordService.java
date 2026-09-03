package com.sport.service.services;

import com.sport.service.entities.TourRecord;
import com.sport.service.repositories.TourRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourRecordService {
    private final TourRecordRepository tourRecordRepository;

    @Transactional(readOnly = true)
    public boolean hasTourBeenShown(Long subscriberId, String route) {
        return tourRecordRepository.existsBySubscriberIdAndRoute(subscriberId, route);
    }

    @Transactional
    public void recordTourShown(Long subscriberId, String route) {
        if (!tourRecordRepository.existsBySubscriberIdAndRoute(subscriberId, route)) {
            TourRecord record = TourRecord.builder()
                .subscriberId(subscriberId)
                .route(route)
                .showedAt(LocalDateTime.now())
                .build();
            tourRecordRepository.save(record);
        }
        log.info("Tour recorded: subscriberId={}, route={}", subscriberId, route);
    }
}