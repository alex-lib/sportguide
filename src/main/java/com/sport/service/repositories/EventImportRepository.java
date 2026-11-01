package com.sport.service.repositories;

import com.sport.service.entities.EventImportRecord;
import com.sport.service.services.eventimport.EventCandidate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventImportRepository extends JpaRepository<EventImportRecord, Long> {
    Optional<EventImportRecord> findBySourceAndExternalKey(
            EventCandidate.Source source, String externalKey);

    @Query(
            "select r from EventImportRecord r where r.source = :source and r.eventId is not null"
                + " and r.lastChecked <= :cutoff and exists (select e.id from Event e where e.id ="
                + " r.eventId and e.date >= :today)")
    List<EventImportRecord> findDue(
            @Param("source") EventCandidate.Source source,
            @Param("cutoff") LocalDate cutoff,
            @Param("today") LocalDate today);

    @Query(
            "select r from EventImportRecord r where r.source = :source and r.announcementQueued ="
                + " false and exists (select e.id from Event e where e.id = r.eventId and e.date"
                + " between :today and :until)")
    List<EventImportRecord> findUnannounced(
            @Param("source") EventCandidate.Source source,
            @Param("today") LocalDate today,
            @Param("until") LocalDate until);

    List<EventImportRecord> findByPendingReportIsNotNull();
}
