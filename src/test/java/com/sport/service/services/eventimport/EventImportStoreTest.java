package com.sport.service.services.eventimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sport.service.entities.Event;
import com.sport.service.entities.EventImportRecord;
import com.sport.service.entities.enums.common.District;
import com.sport.service.repositories.EventImportRepository;
import com.sport.service.repositories.EventRepository;
import com.sport.service.services.NotificationCreatorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

class EventImportStoreTest {
    private final EventImportRepository imports = mock(EventImportRepository.class);
    private final EventRepository events = mock(EventRepository.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final NotificationCreatorService creator = mock(NotificationCreatorService.class);
    private final EventImportMessages messages = mock(EventImportMessages.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final EventImportStore store =
            new EventImportStore(imports, events, jdbc, publisher, creator, messages);
    private final LocalDateTime now = LocalDateTime.of(2026, 11, 1, 9, 0);
    private EventImportRecord saved;

    @BeforeEach
    void setup() {
        when(imports.findBySourceAndExternalKey(any(), any()))
                .thenAnswer(call -> Optional.ofNullable(saved));
        when(imports.save(any()))
                .thenAnswer(
                        call -> {
                            saved = call.getArgument(0);
                            saved.setId(1L);
                            return saved;
                        });
        when(events.saveAndFlush(any()))
                .thenAnswer(
                        call -> {
                            Event event = call.getArgument(0);
                            event.setId(10L);
                            return event;
                        });
        when(creator.createEventNotification(any())).thenReturn("Новое событие");
    }

    @Test
    void fakelImportIsIdempotentAndDoesNotAnnounceImmediately() {
        store.discover(candidate(), now);
        store.discover(candidate(), now.plusDays(1));
        assertThat(saved.getEventId()).isEqualTo(10L);
        verify(events, times(1)).saveAndFlush(any());
        verifyNoInteractions(publisher, messages);
    }

    @Test
    void fakelImportsDistantMatchesButNeverPastEvents() {
        var candidate = candidate();
        candidate.setDate(now.toLocalDate().minusDays(1));
        store.discover(candidate, now);
        candidate.setDate(now.toLocalDate());
        candidate.setTime(LocalTime.of(8, 0));
        store.discover(candidate, now);
        verifyNoInteractions(imports, events, publisher);
        candidate.setDate(now.toLocalDate().plusMonths(5));
        candidate.setTime(null);
        store.discover(candidate, now);
        verify(events).saveAndFlush(argThat(event -> event.getTime() == null));
        assertThat(saved.isManualReview()).isFalse();
        verifyNoInteractions(publisher, messages);
    }

    @Test
    void kassirStillAnnouncesImmediately() {
        var candidate = candidate();
        var kassir =
                new EventCandidate(
                        EventCandidate.Source.KASSIR, "42", "https://vrn.kassir.ru/sport/boxing");
        kassir.setName(candidate.getName());
        kassir.setDescription(candidate.getDescription());
        kassir.setDate(candidate.getDate());
        kassir.setTime(candidate.getTime());
        kassir.setPlaceName(candidate.getPlaceName());
        kassir.setAddress(candidate.getAddress());
        kassir.setDistrict(candidate.getDistrict());
        store.discover(kassir, now);
        verify(publisher).publishEvent(any(Object.class));
        verifyNoInteractions(messages);
    }

    @Test
    void incompleteEventRemainsForManualCreationAndDoesNotAutoPublishLater() {
        var candidate = candidate();
        candidate.setDescription(null);
        store.discover(candidate, now);
        assertThat(saved.isManualReview()).isTrue();
        assertThat(saved.getPendingReport()).contains("Нужно заполнить вручную: описание");
        store.discover(candidate(), now.plusDays(1));
        assertThat(saved.getEventId()).isNull();
        verify(events, never()).saveAndFlush(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void connectsExistingManualEventInsteadOfAnnouncingDuplicate() {
        Event existing =
                Event.builder()
                        .id(42L)
                        .name("Футбольный матч Факел - Динамо")
                        .date(LocalDate.of(2026, 11, 7))
                        .build();
        when(events.findAll()).thenReturn(List.of(existing));
        store.discover(candidate(), now);
        assertThat(saved.getEventId()).isEqualTo(42L);
        verify(events, never()).saveAndFlush(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void seriesLinkDoesNotCollapseDifferentMatchesAndQuotedCityNamesMatchManualTitles() {
        String url = "https://vrn.kassir.ru/sport/domashnie-igryi-hk-buran-v-sezone-2026-2027";
        var candidate = new EventCandidate(EventCandidate.Source.KASSIR, "6224473", url);
        candidate.setName("«Буран» Воронеж — «Торос» Нефтекамск");
        candidate.setDate(LocalDate.of(2026, 11, 4));
        Event other =
                Event.builder()
                        .link(url)
                        .name("Хоккейный матч Буран - Ижсталь")
                        .date(candidate.getDate())
                        .build();
        assertThat(EventImportStore.sameEvent(other, candidate)).isFalse();
        other.setName("Хоккейный матч Буран - Торос");
        assertThat(EventImportStore.sameEvent(other, candidate)).isTrue();
        other.setTime(LocalTime.of(17, 0));
        candidate.setTime(LocalTime.of(19, 0));
        assertThat(EventImportStore.sameEvent(other, candidate)).isFalse();
    }

    @Test
    void refreshUpdatesScheduleAndQueuesChangeOnlyOnce() {
        Event event = prepareRefresh();
        var candidate = candidate();
        candidate.setTime(LocalTime.of(16, 0));
        store.refresh(1L, candidate, now.toLocalDate().plusDays(3));
        assertThat(event.getTime()).isEqualTo(LocalTime.of(16, 0));
        assertThat(saved.getLastChecked()).isEqualTo(now.toLocalDate().plusDays(3));
        assertThat(saved.getPendingReport()).contains("Изменилось расписание", "16:00");
        store.refresh(1L, candidate, now.toLocalDate().plusDays(4));
        verify(messages).replacePending(eq(event), contains("Было: 2026-11-07 14:00"));
        verifyNoInteractions(publisher);
    }

    @Test
    void refreshUpdatesManuallyEditedScheduleAsRequested() {
        Event event = prepareRefresh();
        event.setTime(LocalTime.of(15, 0));
        var candidate = candidate();
        candidate.setTime(LocalTime.of(16, 0));
        store.refresh(1L, candidate, now.toLocalDate().plusDays(3));
        assertThat(event.getTime()).isEqualTo(LocalTime.of(16, 0));
        verify(messages).replacePending(eq(event), contains("Было: 2026-11-07 15:00"));
    }

    @Test
    void failedRefreshKeepsPreviousScheduleAndIsRetried() {
        Event event = prepareRefresh();
        var candidate = candidate();
        candidate.setDate(null);
        store.refresh(1L, candidate, now.toLocalDate().plusDays(3));
        assertThat(event.getTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(saved.getLastChecked()).isEqualTo(now.toLocalDate());
        assertThat(saved.getPendingReport()).contains("Событие сохранено без изменений");
    }

    @Test
    void announcesUpcomingFakelFromDatabaseAndMarksItPersistently() {
        var event = prepareRefresh();
        when(imports.findUnannounced(
                        EventCandidate.Source.FAKEL,
                        now.toLocalDate(),
                        now.toLocalDate().plusDays(7)))
                .thenReturn(List.of(saved))
                .thenReturn(List.of());
        store.queueFakelReminders(now);
        store.queueFakelReminders(now);
        assertThat(saved.isAnnouncementQueued()).isTrue();
        verify(messages).queue(event, "");
        verifyNoInteractions(publisher);
    }

    @Test
    void fillsPreviouslyUnknownTimeAndNotifiesSubscribers() {
        var event = prepareRefresh();
        event.setTime(null);
        saved.setSourceTime(null);
        store.refresh(1L, candidate(), now.toLocalDate().plusDays(4));
        assertThat(event.getTime()).isEqualTo(LocalTime.of(14, 0));
        verify(messages).replacePending(eq(event), contains("Было: 2026-11-07 время уточняется"));
    }

    @Test
    void failedSynchronizationOfExistingMatchDoesNotAskAdminToCreateDuplicate() {
        prepareRefresh();
        var candidate = candidate();
        candidate.getProblems().add("Ошибка сохранения");
        store.failure(candidate);
        assertThat(saved.getPendingReport())
                .contains("отредактируйте существующее событие")
                .doesNotContain("создайте событие вручную");
        assertThat(saved.isManualReview()).isFalse();
    }

    @Test
    void kassirCheckUsesFourDayCutoffAcrossMonthBoundaryAndDatabaseClaimResult() {
        var today = LocalDate.of(2026, 11, 3);
        when(jdbc.update(
                        contains("event_import_source_checks"),
                        eq(today),
                        eq(LocalDate.of(2026, 10, 30))))
                .thenReturn(1, 0);
        assertThat(store.claimKassirCheck(today)).isTrue();
        assertThat(store.claimKassirCheck(today)).isFalse();
    }

    @Test
    void refreshAppliesKassirPresentationRulesWithoutScheduleChangeNotification() {
        var event = prepareRefresh();
        event.setName("«Буран» Воронеж — «ЦСК ВВС» Самара");
        event.setDescription("Старая аннотация клуба");
        event.setAddress("г. Воронеж, ул. Карла Маркса, 116");
        var candidate =
                new EventCandidate(
                        EventCandidate.Source.KASSIR, "1", "https://vrn.kassir.ru/sport/buran");
        candidate.setName(event.getName());
        candidate.setDescription(event.getDescription());
        candidate.setAddress(event.getAddress());
        candidate.setDate(event.getDate());
        candidate.setTime(event.getTime());
        EventImportRules.apply(candidate);
        store.refresh(1L, candidate, now.toLocalDate().plusDays(4));
        assertThat(event.getName()).isEqualTo("Хоккейный матч «Буран» Воронеж — «ЦСК ВВС» Самара");
        assertThat(event.getDescription()).isEqualTo("Матч в рамках чемпионата ВХЛ.");
        assertThat(event.getAddress()).isEqualTo("ул. Карла Маркса, 116");
        verifyNoInteractions(messages, publisher);
    }

    private Event prepareRefresh() {
        saved = new EventImportRecord();
        saved.setId(1L);
        saved.setEventId(10L);
        saved.setSourceDate(candidate().getDate());
        saved.setSourceTime(LocalTime.of(14, 0));
        saved.setLastChecked(now.toLocalDate());
        Event event =
                Event.builder()
                        .id(10L)
                        .name(candidate().getName())
                        .date(candidate().getDate())
                        .time(LocalTime.of(14, 0))
                        .build();
        when(imports.findById(1L)).thenReturn(Optional.of(saved));
        when(events.findById(10L)).thenReturn(Optional.of(event));
        return event;
    }

    static EventCandidate candidate() {
        var event =
                new EventCandidate(
                        EventCandidate.Source.FAKEL, "2997", "https://fakelfc.ru/matches/2997");
        event.setName("Футбольный матч Факел — Динамо");
        event.setDescription("Премьер-лига, 14 тур");
        event.setDate(LocalDate.of(2026, 11, 7));
        event.setTime(LocalTime.of(14, 0));
        event.setAddress("ул. Писателя Маршака, 1г");
        event.setPlaceName("Стадион Факел");
        event.setDistrict(District.SOVETSKYY);
        return event;
    }
}
