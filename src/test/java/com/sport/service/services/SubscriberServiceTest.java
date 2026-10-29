package com.sport.service.services;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.entities.Subscriber;
import com.sport.service.repositories.SubscriberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;

class SubscriberServiceTest {
    private final SubscriberRepository repository = mock(SubscriberRepository.class);
    private final SubscriberService service = new SubscriberService(repository,
            mock(TelegramMessageSender.class), mock(NotificationSenderService.class), mock(NotificationCreatorService.class));

    @ParameterizedTest
    @ValueSource(strings = {"@Coach_Name", "Coach_Name", "  @Coach_Name  "})
    void resolvesUsernameToExistingTelegramIdIgnoringCase(String input) {
        Subscriber subscriber = Subscriber.builder().id(99L).username("coach_name").build();
        when(repository.findAllByUsernameIgnoreCase("Coach_Name")).thenReturn(List.of(subscriber));
        Subscriber found = service.findByUsername(input).orElseThrow();
        assertEquals(99L, found.getId());
        verify(repository).findAllByUsernameIgnoreCase("Coach_Name");
        verify(repository, never()).findById(anyLong());
    }

    @Test
    void unknownUsernameDoesNotCreateSubscriber() {
        assertTrue(service.findByUsername("@unknown_coach").isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void ambiguousUsernameCannotSelectWrongSubscriber() {
        when(repository.findAllByUsernameIgnoreCase("coach_name")).thenReturn(List.of(
                Subscriber.builder().id(99L).build(), Subscriber.builder().id(100L).build()));
        assertThrows(IllegalArgumentException.class, () -> service.findByUsername("@coach_name"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "@", "@@coach_name", "two names", "https://t.me/coach_name", "имя"})
    void malformedUsernameDoesNotQueryDatabase(String input) {
        assertThrows(IllegalArgumentException.class, () -> service.findByUsername(input));
        verifyNoInteractions(repository);
    }
}
