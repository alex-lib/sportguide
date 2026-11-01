package com.sport.service.services.eventimport;

import com.sport.service.entities.Event;
import com.sport.service.entities.EventImportRecord;
import com.sport.service.repositories.EventImportRepository;
import com.sport.service.repositories.EventRepository;
import com.sport.service.services.NotificationCreatorService;
import com.sport.service.store.notifications.Notification;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventImportStore {
    private final EventImportRepository imports;
    private final EventRepository events;
    private final JdbcTemplate jdbc;
    private final ApplicationEventPublisher notifications;
    private final NotificationCreatorService notificationCreator;
    private final EventImportMessages messages;

    @Transactional
    public void discover(EventCandidate candidate, LocalDateTime now) {
        if (candidate.getDate() != null) {
            if (candidate.getDate().isBefore(now.toLocalDate())) return;
            if (candidate.getDate().equals(now.toLocalDate())
                    && candidate.getTime() != null
                    && !candidate.getTime().isAfter(now.toLocalTime())) return;
        }
        lock();
        EventImportRecord record = record(candidate);
        if (record.getEventId() != null) return;
        List<Event> matches =
                events.findAll().stream().filter(event -> sameEvent(event, candidate)).toList();
        if (matches.size() == 1) {
            attach(record, matches.getFirst(), candidate, now.toLocalDate());
        } else if (matches.size() > 1) {
            candidate
                    .getProblems()
                    .add(
                            "В приложении найдено несколько похожих событий; требуется ручное"
                                    + " сопоставление");
            report(record, candidate);
        } else if (!candidate.readyToImport()) {
            report(record, candidate);
        } else if (!record.isManualReview()) {
            Event event =
                    Event.builder()
                            .name(candidate.getName())
                            .description(candidate.getDescription())
                            .date(candidate.getDate())
                            .time(candidate.getTime())
                            .placeName(candidate.getPlaceName())
                            .district(candidate.getDistrict())
                            .address(candidate.getAddress())
                            .link(candidate.getLink())
                            .build();
            events.saveAndFlush(event);
            attach(record, event, candidate, now.toLocalDate());
            if (candidate.getSource() != EventCandidate.Source.FAKEL) {
                notifications.publishEvent(
                        Notification.builder()
                                .type(Notification.NotificationType.EVENT)
                                .message(notificationCreator.createEventNotification(event))
                                .build());
            }
        } else {
            candidate
                    .getProblems()
                    .add("Событие уже передано для ручного создания; данные источника обновлены");
            report(record, candidate);
        }
        imports.save(record);
    }

    @Transactional
    public void syncFakel(EventCandidate candidate, LocalDateTime now) {
        lock();
        var existing =
                imports.findBySourceAndExternalKey(
                        candidate.getSource(), candidate.getExternalId());
        if (existing.isPresent() && existing.get().getEventId() != null) {
            refresh(existing.get().getId(), candidate, now.toLocalDate());
        } else {
            discover(candidate, now);
        }
    }

    @Transactional
    public boolean claimKassirCheck(LocalDate today) {
        return jdbc.update(
                        "INSERT INTO event_import_source_checks(source, last_attempt) VALUES"
                            + " ('KASSIR', ?) ON CONFLICT (source) DO UPDATE SET last_attempt ="
                            + " EXCLUDED.last_attempt WHERE event_import_source_checks.last_attempt"
                            + " <= ?",
                        today,
                        today.minusDays(4))
                == 1;
    }

    @Transactional
    public boolean claimFakelCalendar(int competition, LocalDate today) {
        // Database state keeps the four-day interval across restarts and concurrent instances.
        return jdbc.update(
                        "INSERT INTO event_import_calendars(competition, last_attempt) VALUES (?,"
                            + " ?) ON CONFLICT (competition) DO UPDATE SET last_attempt ="
                            + " EXCLUDED.last_attempt WHERE event_import_calendars.last_attempt <="
                            + " ?",
                        competition,
                        today,
                        today.minusDays(4))
                == 1;
    }

    @Transactional
    public void queueFakelReminders(LocalDateTime now) {
        lock();
        for (EventImportRecord record :
                imports.findUnannounced(
                        EventCandidate.Source.FAKEL,
                        now.toLocalDate(),
                        now.toLocalDate().plusDays(7))) {
            Event event = events.findById(record.getEventId()).orElse(null);
            if (event == null
                    || (event.getDate().equals(now.toLocalDate())
                            && event.getTime() != null
                            && !event.getTime().isAfter(now.toLocalTime()))) continue;
            messages.queue(event, "Скоро домашний матч «Факела»!\n\n");
            record.setAnnouncementQueued(true);
        }
    }

    @Transactional
    public void refresh(Long recordId, EventCandidate candidate, LocalDate today) {
        lock();
        EventImportRecord record = imports.findById(recordId).orElseThrow();
        Event event = events.findById(record.getEventId()).orElse(null);
        if (event == null) return;
        boolean fakel = candidate.getSource() == EventCandidate.Source.FAKEL;
        if (candidate.getDate() == null
                || (!fakel && candidate.getTime() == null)
                || !candidate.getProblems().isEmpty()
                || (fakel && !candidate.readyToImport())) {
            candidate.getCollected().put("Название в приложении", event.getName());
            candidate
                    .getCollected()
                    .put("Начало в приложении", schedule(event.getDate(), event.getTime()));
            record.setPendingReport(EventImportReport.existing(candidate));
            return;
        }
        boolean changed =
                !Objects.equals(candidate.getDate(), event.getDate())
                        || !Objects.equals(candidate.getTime(), event.getTime());
        String previous = schedule(event.getDate(), event.getTime());
        event.setDate(candidate.getDate());
        event.setTime(candidate.getTime());
        if (fakel) {
            event.setName(candidate.getName());
            event.setDescription(candidate.getDescription());
            event.setPlaceName(candidate.getPlaceName());
            event.setAddress(candidate.getAddress());
            event.setDistrict(candidate.getDistrict());
            event.setLink(candidate.getLink());
        }
        if (changed) {
            String change =
                    "Изменилось расписание события.\nБыло: "
                            + previous
                            + "\nСтало: "
                            + schedule(event.getDate(), event.getTime())
                            + "\n\n";
            messages.replacePending(event, change);
            record.setPendingReport(change + event.getName() + "\n" + record.getSourceUrl());
        }
        record.setSourceDate(candidate.getDate());
        record.setSourceTime(candidate.getTime());
        record.setLastChecked(today);
    }

    private static String schedule(LocalDate date, java.time.LocalTime time) {
        return date + " " + (time == null ? "время уточняется" : time);
    }

    @Transactional
    public void failure(EventCandidate candidate) {
        lock();
        EventImportRecord record = record(candidate);
        if (record.getEventId() == null) report(record, candidate);
        else record.setPendingReport(EventImportReport.existing(candidate));
        imports.save(record);
    }

    @Transactional
    public void delivered(Long id, String report) {
        lock();
        EventImportRecord record = imports.findById(id).orElseThrow();
        record.setDeliveredReportHash(EventImportReport.hash(report));
        if (Objects.equals(record.getPendingReport(), report)) record.setPendingReport(null);
    }

    private void lock() {
        // Serializes concurrent import jobs across application instances, only during short DB
        // writes.
        jdbc.execute("SELECT pg_advisory_xact_lock(73410521)");
    }

    private EventImportRecord record(EventCandidate candidate) {
        String key =
                EventCandidate.blank(candidate.getExternalId())
                        ? "unknown:"
                                + EventImportReport.hash(
                                        candidate.getLink() + "|" + candidate.getName())
                        : candidate.getExternalId();
        return imports.findBySourceAndExternalKey(candidate.getSource(), key)
                .orElseGet(
                        () -> {
                            EventImportRecord record = new EventImportRecord();
                            record.setSource(candidate.getSource());
                            record.setExternalKey(key);
                            record.setSourceUrl(candidate.getLink());
                            return record;
                        });
    }

    private void attach(
            EventImportRecord record, Event event, EventCandidate candidate, LocalDate today) {
        record.setEventId(event.getId());
        record.setSourceDate(candidate.getDate());
        record.setSourceTime(candidate.getTime());
        record.setLastChecked(today);
        record.setPendingReport(null);
    }

    private void report(EventImportRecord record, EventCandidate candidate) {
        record.setManualReview(true);
        record.setPendingReport(EventImportReport.incomplete(candidate));
    }

    static boolean sameEvent(Event event, EventCandidate candidate) {
        if (Objects.equals(event.getLink(), candidate.getLink())
                && !EventCandidate.blank(candidate.getLink())) {
            // A shared Kassir series/season URL alone must never collapse different matches.
            if (candidate.getSource() == EventCandidate.Source.FAKEL
                    || candidate.getLink().contains("#")) return true;
        }
        return candidate.getDate() != null
                && Objects.equals(event.getDate(), candidate.getDate())
                && (event.getTime() == null
                        || candidate.getTime() == null
                        || Objects.equals(event.getTime(), candidate.getTime()))
                && !EventCandidate.blank(candidate.getName())
                && EventImportRules.matchName(event.getName())
                        .equals(EventImportRules.matchName(candidate.getName()));
    }
}
