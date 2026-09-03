package com.sport.service.repositories;

import com.sport.service.entities.TourRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourRecordRepository extends JpaRepository<TourRecord, Long> {

    boolean existsBySubscriberIdAndRoute(Long subscriberId, String route);

}