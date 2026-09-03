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
        log.info("TourRecordService.hasTourBeenShown called: subscriberId={}, route={}", subscriberId, route);
        boolean result = tourRecordRepository.existsBySubscriberIdAndRoute(subscriberId, route);
        log.info("TourRecordService.hasTourBeenShown: subscriberId={}, route={}, exists={}", subscriberId, route, result);
        return result;
    }

    @Transactional
    public void recordTourShown(Long subscriberId, String route) {
        log.info("TourRecordService.recordTourShown called: subscriberId={}, route={}", subscriberId, route);
        if (!tourRecordRepository.existsBySubscriberIdAndRoute(subscriberId, route)) {
            TourRecord record = TourRecord.builder()
                .subscriberId(subscriberId)
                .route(route)
                .showedAt(LocalDateTime.now())
                .build();
            tourRecordRepository.save(record);
            log.info("TourRecordService.recordTourShown: saved new record for subscriberId={}, route={}", subscriberId, route);
        } else {
            log.info("TourRecordService.recordTourShown: record already exists for subscriberId={}, route={}", subscriberId, route);
        }
    }
}