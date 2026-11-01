package com.sport.service.services.eventimport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sport.service.constants.Constants;
import com.sport.service.entities.EventImportRecord;
import com.sport.service.repositories.EventImportRepository;
import com.sport.service.store.notifications.Notification;
import com.sport.service.store.notifications.NotificationPublisher;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

class EventImportSchedulerTest {
    private final EventSourceClient client = mock(EventSourceClient.class);
    private final KassirEventParser kassir = mock(KassirEventParser.class);
    private final FakelEventParser fakel = mock(FakelEventParser.class);
    private final EventImportStore store = mock(EventImportStore.class);
    private final EventImportRepository imports = mock(EventImportRepository.class);
    private final NotificationPublisher publisher = mock(NotificationPublisher.class);
    private final EventImportMessages messages = mock(EventImportMessages.class);
    private final EventImportScheduler scheduler =
            new EventImportScheduler(
                    client, kassir, fakel, store, imports, publisher, messages, 42L);

    @Test
    void checksAllThreeTournamentsWhenFourDayIntervalIsDue() throws Exception {
        when(store.claimFakelCalendar(anyInt(), any())).thenReturn(true);
        for (int id : List.of(32, 2, 9))
            when(client.fakelTournament(id)).thenReturn("calendar" + id);
        var candidate = EventImportStoreTest.candidate();
        when(fakel.parse(eq("calendar9"), any())).thenReturn(List.of(candidate));
        scheduler.discover();
        for (int id : List.of(32, 2, 9)) verify(client).fakelTournament(id);
        verify(store).syncFakel(eq(candidate), any());
    }

    @Test
    void dailyJobDoesNotRequestEitherSourceBeforePersistedIntervalIsDue() throws Exception {
        when(store.claimFakelCalendar(anyInt(), any())).thenReturn(false);
        scheduler.discover();
        scheduler.discover();
        verify(client, never()).fakelTournament(anyInt());
        verifyNoInteractions(client);
        verify(imports, never()).findDue(any(), any(), any());
    }

    @Test
    void tournamentFailureDoesNotStopOtherTournaments() throws Exception {
        when(store.claimFakelCalendar(anyInt(), any())).thenReturn(true);
        when(client.fakelTournament(32)).thenThrow(new IOException("challenge"));
        when(client.fakelTournament(2)).thenReturn("cup");
        when(client.fakelTournament(9)).thenReturn("friendly");
        var candidate = EventImportStoreTest.candidate();
        when(fakel.parse(eq("cup"), any())).thenReturn(List.of(candidate));
        scheduler.discover();
        verify(store).syncFakel(eq(candidate), any());
        verify(client).fakelTournament(9);
        verify(store)
                .failure(
                        argThat(
                                e ->
                                        e.getLink()
                                                .equals(
                                                        FakelEventParser.LIST_URL
                                                                + "?competition=32")));
    }

    @Test
    void fourDayCheckRefreshesKassirEvenWhenListingFails() throws Exception {
        var today = LocalDate.now(ZoneId.of(Constants.TIME_ZONE));
        var record = new EventImportRecord();
        record.setId(7L);
        record.setExternalKey("6224");
        record.setSource(EventCandidate.Source.KASSIR);
        record.setSourceUrl("https://vrn.kassir.ru/sport/boxing#6224");
        when(imports.findDue(EventCandidate.Source.KASSIR, today.minusDays(4), today))
                .thenReturn(List.of(record));
        when(client.kassir("https://vrn.kassir.ru/sport/boxing")).thenReturn("card");
        var candidate =
                new EventCandidate(EventCandidate.Source.KASSIR, "6224", record.getSourceUrl());
        when(kassir.detail(eq("card"), any())).thenReturn(List.of(candidate));
        when(store.claimKassirCheck(today)).thenReturn(true);
        when(client.kassirListing()).thenThrow(new IOException("catalog unavailable"));
        scheduler.discover();
        verify(store).refresh(7L, candidate, today);
        verify(client, never()).fakelTournament(anyInt());
    }

    @Test
    void kassirDiscoveryAndRefreshShareOnePersistedFourDayGate() throws Exception {
        var today = LocalDate.now(ZoneId.of(Constants.TIME_ZONE));
        when(store.claimKassirCheck(today)).thenReturn(true, false);
        when(client.kassirListing()).thenReturn("listing");
        var candidate =
                new EventCandidate(
                        EventCandidate.Source.KASSIR,
                        "boxing",
                        "https://vrn.kassir.ru/sport/boxing");
        when(kassir.listing("listing")).thenReturn(List.of(candidate));
        when(client.kassir(candidate.getLink())).thenReturn("card");
        when(kassir.detail("card", candidate)).thenReturn(List.of(candidate));
        scheduler.discover();
        scheduler.discover();
        verify(client).kassirListing();
        verify(client).kassir(candidate.getLink());
        verify(store).discover(eq(candidate), any());
        verify(imports).findDue(EventCandidate.Source.KASSIR, today.minusDays(4), today);
    }

    @Test
    void failedKassirAttemptDoesNotCauseDailyRetriesOrBlockFakel() throws Exception {
        var today = LocalDate.now(ZoneId.of(Constants.TIME_ZONE));
        when(store.claimKassirCheck(today)).thenReturn(true, false);
        when(client.kassirListing()).thenThrow(new IOException("timeout"));
        when(store.claimFakelCalendar(eq(9), any())).thenReturn(true, false);
        when(client.fakelTournament(9)).thenReturn("friendly");
        scheduler.discover();
        scheduler.discover();
        verify(client).kassirListing();
        verify(client).fakelTournament(9);
        verify(imports).findDue(EventCandidate.Source.KASSIR, today.minusDays(4), today);
        verify(store).failure(argThat(event -> event.getSource() == EventCandidate.Source.KASSIR));
    }

    @Test
    void remindersUseSavedEventsWithoutAnySourceRequests() {
        scheduler.remindFakel();
        verify(store).queueFakelReminders(any());
        verify(messages).sendPending();
        verifyNoInteractions(client);
    }

    @Test
    void startupChecksFourDayGateAndQueuesUpcomingReminders() {
        scheduler.initializeSources();
        for (int id : List.of(32, 2, 9)) verify(store).claimFakelCalendar(eq(id), any());
        verify(store).queueFakelReminders(any());
        verifyNoInteractions(client);
    }

    @Test
    void unchangedReportIsNotRepeatedToAdmin() {
        var record = reportRecord();
        record.setDeliveredReportHash(EventImportReport.hash(record.getPendingReport()));
        when(imports.findByPendingReportIsNotNull()).thenReturn(List.of(record));
        scheduler.sendReports();
        verifyNoInteractions(publisher);
        verify(store).delivered(record.getId(), record.getPendingReport());
    }

    @Test
    void reportsOnlyToMainAdminAndRetainsFailedDeliveryForRetry() {
        var record = reportRecord();
        when(imports.findByPendingReportIsNotNull()).thenReturn(List.of(record));
        doThrow(new RuntimeException("Redis down")).when(publisher).publish(any());
        scheduler.sendReports();
        verify(publisher)
                .publish(
                        argThat(
                                message ->
                                        message.getUserId().equals(42L)
                                                && message.getType()
                                                        == Notification.NotificationType.ADMIN_ALERT
                                                && message.getId() != null));
        verify(store, never()).delivered(any(), any());
    }

    private EventImportRecord reportRecord() {
        var record = new EventImportRecord();
        record.setId(7L);
        record.setPendingReport("Название: Бокс\nНужно заполнить вручную: время");
        return record;
    }
}
