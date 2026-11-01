package com.sport.service.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sport.service.entities.Event;
import com.sport.service.entities.enums.common.District;
import com.sport.service.mappers.event.EventMapper;
import com.sport.service.repositories.EventRepository;
import com.sport.service.web.models.event.EventEditRequest;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

class EventEditingServiceTest {
    @Test
    void savesFieldsWithoutCreatingAnotherEventOrNotifyingSubscribers() {
        var repository = mock(EventRepository.class);
        var publisher = mock(ApplicationEventPublisher.class);
        var service =
                new EventService(
                        repository,
                        mock(NotificationCreatorService.class),
                        publisher,
                        mock(EventMapper.class));
        var event = Event.builder().id(1L).version(0L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(event));
        var request =
                new EventEditRequest(
                        0L,
                        " Матч ",
                        "Описание",
                        LocalDate.of(2026, 11, 7),
                        LocalTime.of(15, 30),
                        District.CENTRALNYY,
                        "Адрес",
                        "Место",
                        "https://example.com",
                        "51, 39");
        var response = service.updateEvent(1L, request);
        assertThat(response.name()).isEqualTo("Матч");
        assertThat(response.time()).isEqualTo(LocalTime.of(15, 30));
        assertThat(response.coordinates()).isEqualTo("51, 39");
        verify(repository).flush();
        verifyNoInteractions(publisher);
        event.setVersion(1L);
        assertThatThrownBy(() -> service.updateEvent(1L, request))
                .isInstanceOfSatisfying(
                        ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode().value()).isEqualTo(409));
        assertThatThrownBy(() -> service.getForEditing(999L))
                .isInstanceOfSatisfying(
                        ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode().value()).isEqualTo(404));
    }
}
