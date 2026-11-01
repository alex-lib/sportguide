package com.sport.service.services.eventimport;

import com.sport.service.constants.Constants;
import com.sport.service.entities.EventImportRecord;
import com.sport.service.repositories.EventImportRepository;
import com.sport.service.store.notifications.Notification;
import com.sport.service.store.notifications.NotificationPublisher;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.event-import.enabled", havingValue = "true")
public class EventImportScheduler {
    private final EventSourceClient client;
    private final KassirEventParser kassir;
    private final FakelEventParser fakel;
    private final EventImportStore store;
    private final EventImportRepository imports;
    private final NotificationPublisher publisher;
    private final Long adminId;
    private final EventImportMessages messages;
    static final List<Integer> FAKEL_TOURNAMENTS = List.of(32, 2, 9);

    public EventImportScheduler(
            EventSourceClient client,
            KassirEventParser kassir,
            FakelEventParser fakel,
            EventImportStore store,
            EventImportRepository imports,
            NotificationPublisher publisher,
            EventImportMessages messages,
            @Value("${telegram.mainAdminId}") Long adminId) {
        this.client = client;
        this.kassir = kassir;
        this.fakel = fakel;
        this.store = store;
        this.imports = imports;
        this.publisher = publisher;
        this.adminId = adminId;
        this.messages = messages;
    }

    @Scheduled(cron = "${app.event-import.discovery-cron:0 0 9 * * *}", zone = Constants.TIME_ZONE)
    public void discover() {
        checkKassir();
        checkFakelCalendars();
        sendReports();
    }

    private void checkKassir() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(Constants.TIME_ZONE));
        try {
            if (!store.claimKassirCheck(now.toLocalDate())) return;
        } catch (Exception e) {
            log.error("Cannot claim Kassir check", e);
            return;
        }
        try {
            for (EventCandidate card : kassir.listing(client.kassirListing())) {
                try {
                    for (EventCandidate event : kassir.detail(client.kassir(card.getLink()), card))
                        discoverOne(event, now);
                } catch (Exception e) {
                    card.getProblems()
                            .add("Не удалось загрузить карточку: " + e.getClass().getSimpleName());
                    EventImportRules.apply(card);
                    store.failure(card);
                }
            }
        } catch (Exception e) {
            sourceFailure(EventCandidate.Source.KASSIR, KassirEventParser.LIST_URL, e);
        }
        refreshKassir(now.toLocalDate());
    }

    private void discoverOne(EventCandidate event, LocalDateTime now) {
        try {
            store.discover(event, now);
        } catch (Exception e) {
            log.error("Cannot import {} {}", event.getSource(), event.getExternalId(), e);
            event.getProblems().add("Ошибка сохранения: " + e.getClass().getSimpleName());
            store.failure(event);
        }
    }

    private void refreshKassir(LocalDate today) {
        Map<String, List<EventCandidate>> pages = new HashMap<>();
        Map<String, Exception> failures = new HashMap<>();
        for (EventImportRecord record :
                imports.findDue(EventCandidate.Source.KASSIR, today.minusDays(4), today)) {
            String page = record.getSourceUrl().split("#", 2)[0];
            EventCandidate fallback =
                    new EventCandidate(
                            record.getSource(), record.getExternalKey(), record.getSourceUrl());
            try {
                if (failures.containsKey(page)) throw failures.get(page);
                if (!pages.containsKey(page)) {
                    try {
                        pages.put(page, kassir.detail(client.kassir(page), fallback));
                    } catch (Exception e) {
                        failures.put(page, e);
                        throw e;
                    }
                }
                EventCandidate candidate =
                        pages.get(page).stream()
                                .filter(
                                        item ->
                                                record.getExternalKey()
                                                        .equals(item.getExternalId()))
                                .findFirst()
                                .orElse(fallback);
                if (candidate == fallback)
                    fallback.getProblems()
                            .add("Сеанс не найден в источнике; это не подтверждает отмену");
                store.refresh(record.getId(), candidate, today);
            } catch (Exception e) {
                log.error("Cannot refresh event import {}", record.getId(), e);
                fallback.getProblems()
                        .add("Ошибка проверки расписания: " + e.getClass().getSimpleName());
                store.refresh(record.getId(), fallback, today);
            }
        }
    }

    @org.springframework.context.event.EventListener(
            org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void initializeSources() {
        discover();
        remindFakel();
    }

    void checkFakelCalendars() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(Constants.TIME_ZONE));
        for (int competition : FAKEL_TOURNAMENTS) {
            try {
                if (!store.claimFakelCalendar(competition, now.toLocalDate())) continue;
                for (EventCandidate event :
                        fakel.parse(client.fakelTournament(competition), now.toLocalDate())) {
                    try {
                        store.syncFakel(event, now);
                    } catch (Exception e) {
                        event.getProblems()
                                .add("Ошибка сохранения: " + e.getClass().getSimpleName());
                        store.failure(event);
                    }
                }
            } catch (Exception e) {
                sourceFailure(
                        EventCandidate.Source.FAKEL,
                        FakelEventParser.LIST_URL + "?competition=" + competition,
                        e);
            }
        }
    }

    @Scheduled(
            cron = "${app.event-import.fakel-reminder-cron:0 0 10 * * *}",
            zone = Constants.TIME_ZONE)
    public void remindFakel() {
        store.queueFakelReminders(LocalDateTime.now(ZoneId.of(Constants.TIME_ZONE)));
        messages.sendPending();
    }

    private void sourceFailure(EventCandidate.Source source, String url, Exception error) {
        log.error("Cannot read event source {}", source, error);
        EventCandidate failure = new EventCandidate(source, "listing:" + url, url);
        failure.getProblems()
                .add(
                        "Не удалось получить список событий: "
                                + error.getClass().getSimpleName()
                                + (error.getMessage() == null ? "" : ": " + error.getMessage())
                                + ". Проверьте страницу вручную; отдельные события пока"
                                + " неизвестны.");
        store.failure(failure);
    }

    @Scheduled(
            fixedDelayString = "${app.event-import.report-retry-ms:300000}",
            initialDelay = 60000)
    public void sendReports() {
        messages.sendPending();
        for (EventImportRecord record : imports.findByPendingReportIsNotNull()) {
            String report = record.getPendingReport();
            String hash = EventImportReport.hash(report);
            try {
                if (!hash.equals(record.getDeliveredReportHash())) {
                    List<String> parts = EventImportReport.telegramParts(report);
                    for (int i = 0; i < parts.size(); i++) {
                        String key = record.getId() + ":" + hash + ":" + i;
                        publisher.publish(
                                Notification.builder()
                                        .id(
                                                UUID.nameUUIDFromBytes(
                                                        key.getBytes(StandardCharsets.UTF_8)))
                                        .userId(adminId)
                                        .type(Notification.NotificationType.ADMIN_ALERT)
                                        .message(parts.get(i))
                                        .build());
                    }
                }
                store.delivered(record.getId(), report);
            } catch (Exception e) {
                log.error("Cannot queue event report {} for admin; will retry", record.getId(), e);
            }
        }
    }
}
