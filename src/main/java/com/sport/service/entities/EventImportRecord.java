package com.sport.service.entities;

import com.sport.service.services.eventimport.EventCandidate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "event_import_records",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_event_import_source_key",
                        columnNames = {"source", "external_key"}))
@Getter
@Setter
@NoArgsConstructor
public class EventImportRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private EventCandidate.Source source;

    @Column(name = "external_key", nullable = false, length = 1000)
    private String externalKey;

    @Column(name = "source_url", nullable = false, columnDefinition = "text")
    private String sourceUrl;

    // Deliberately no FK: retain identity even after an expired event is removed.
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "manual_review", nullable = false)
    private boolean manualReview;

    @Column(name = "last_checked")
    private LocalDate lastChecked;

    @Column(name = "source_date")
    private LocalDate sourceDate;

    @Column(name = "source_time")
    private LocalTime sourceTime;

    @Column(name = "announcement_queued", nullable = false)
    private boolean announcementQueued;

    @Column(name = "pending_report", columnDefinition = "text")
    private String pendingReport;

    @Column(name = "delivered_report_hash", length = 64)
    private String deliveredReportHash;
}
