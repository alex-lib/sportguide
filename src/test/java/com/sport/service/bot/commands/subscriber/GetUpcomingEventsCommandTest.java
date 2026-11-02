package com.sport.service.bot.commands.subscriber;

import static org.mockito.Mockito.*;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.entities.Event;
import com.sport.service.mappers.event.EventMapper;
import com.sport.service.repositories.EventRepository;
import com.sport.service.services.EventService;
import com.sport.service.services.NotificationCreatorService;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

class GetUpcomingEventsCommandTest {
    // UTC is still October 31, while Moscow is already November 1, 01:00.
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-31T22:00:00Z"), ZoneOffset.UTC);
    private final EventRepository repository = mock(EventRepository.class);
    private final NotificationCreatorService notifications = mock(NotificationCreatorService.class);
    private final TelegramMessageSender sender = mock(TelegramMessageSender.class);
    private final EventService service =
            new EventService(
                    repository,
                    notifications,
                    mock(ApplicationEventPublisher.class),
                    mock(EventMapper.class),
                    clock);
    private final GetUpcomingEventsCommand command =
            new GetUpcomingEventsCommand(service, notifications, sender);

    @Test
    void sendsOnlyEventsWithinNextSevenDaysInChronologicalOrder() {
        var today = LocalDate.of(2026, 11, 1);
        var past = event("Уже началось", today, LocalTime.of(0, 59));
        var now = event("Сейчас", today, LocalTime.of(1, 0));
        var tomorrow = event("Завтра", today.plusDays(1), LocalTime.NOON);
        var unknownTime = event("Время уточняется", today.plusDays(3), null);
        var boundary = event("Через 7 дней", today.plusDays(7), LocalTime.of(1, 0));
        var tooLate = event("Позже 7 дней", today.plusDays(7), LocalTime.of(1, 1));
        when(repository.findAllByDateBetweenOrderByDateAscTimeAsc(today, today.plusDays(7)))
                .thenReturn(List.of(past, now, tomorrow, unknownTime, boundary, tooLate));
        for (var event : List.of(now, tomorrow, unknownTime, boundary)) {
            when(notifications.createEventNotification(event)).thenReturn(event.getName());
        }

        command.processMessage(null, message(), new String[0]);

        var order = inOrder(sender);
        order.verify(sender).sendMessageWithoutPhoto(10L, "Сейчас");
        order.verify(sender).sendMessageWithoutPhoto(10L, "Завтра");
        order.verify(sender).sendMessageWithoutPhoto(10L, "Время уточняется");
        order.verify(sender).sendMessageWithoutPhoto(10L, "Через 7 дней");
        verifyNoMoreInteractions(sender);
        verify(notifications, never()).createEventNotification(past);
        verify(notifications, never()).createEventNotification(tooLate);
        verify(repository).findAllByDateBetweenOrderByDateAscTimeAsc(today, today.plusDays(7));
        verify(repository, never()).findAll();
    }

    @Test
    void sendsEmptyMessageWhenThereAreNoEventsInDateRange() {
        command.processMessage(null, message(), new String[0]);

        verify(sender).sendMessageWithoutPhoto(10L, CommandsConstants.THERE_ARE_NO_EVENTS);
        verifyNoMoreInteractions(sender);
        verifyNoInteractions(notifications);
    }

    @Test
    void sendsEmptyMessageWhenTodaysEventsHaveAlreadyStarted() {
        var today = LocalDate.of(2026, 11, 1);
        when(repository.findAllByDateBetweenOrderByDateAscTimeAsc(today, today.plusDays(7)))
                .thenReturn(List.of(event("Прошедшее", today, LocalTime.MIDNIGHT)));

        command.processMessage(null, message(), new String[0]);

        verify(sender).sendMessageWithoutPhoto(10L, CommandsConstants.THERE_ARE_NO_EVENTS);
        verifyNoMoreInteractions(sender);
        verifyNoInteractions(notifications);
    }

    private static Event event(String name, LocalDate date, LocalTime time) {
        return Event.builder().name(name).date(date).time(time).build();
    }

    private static Message message() {
        var user = new User();
        user.setId(10L);
        var message = new Message();
        message.setFrom(user);
        return message;
    }
}
