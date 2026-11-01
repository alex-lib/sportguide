package com.sport.service.services;

import com.sport.service.constants.Constants;
import com.sport.service.dto.EventDto;
import com.sport.service.entities.Event;
import com.sport.service.entities.enums.common.District;
import com.sport.service.mappers.event.EventMapper;
import com.sport.service.repositories.EventRepository;
import com.sport.service.store.notifications.Notification;
import com.sport.service.web.models.event.EventEditRequest;
import com.sport.service.web.models.event.EventEditResponse;
import com.sport.service.web.models.event.ListEventResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final NotificationCreatorService notificationCreatorService;
    private final ApplicationEventPublisher events;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    public EventEditResponse getForEditing(Long id) {
        return EventEditResponse.from(requiredEvent(id));
    }

    @Transactional
    public EventEditResponse updateEvent(Long id, EventEditRequest request) {
        Event event = requiredEvent(id);
        if (!Objects.equals(event.getVersion(), request.version())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Событие уже изменилось. Обновите данные и повторите сохранение.");
        }
        event.setName(request.name().strip());
        event.setDescription(request.description().strip());
        event.setDate(request.date());
        event.setTime(request.time());
        event.setDistrict(request.district());
        event.setAddress(request.address().strip());
        event.setPlaceName(request.placeName().strip());
        event.setLink(
                request.link() == null || request.link().isBlank() ? null : request.link().strip());
        event.setCoordinates(
                request.coordinates() == null || request.coordinates().isBlank()
                        ? null
                        : request.coordinates().strip());
        eventRepository.flush();
        return EventEditResponse.from(event);
    }

    private Event requiredEvent(Long id) {
        return eventRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Событие не найдено"));
    }

    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }

    public ListEventResponse findAllEventsWithFilter(
            String districtStr, String dateStr, String search) {
        LocalDate date = null;

        District district;
        if (districtStr == null || districtStr.isEmpty() || districtStr.equals("ALL_DISTRICTS")) {
            district = null;
        } else {
            district = District.valueOf(districtStr);
        }

        if (dateStr != null && !dateStr.isEmpty()) {
            date = LocalDate.parse(dateStr);
        }

        var result = eventRepository.findWithFilters(district, date, search);
        return eventMapper.listEventToListEventResponse(result);
    }

    @Transactional
    public void createEvent(EventDto dto) {
        Event event = eventMapper.eventDtoToEvent(dto);
        eventRepository.save(event);
        String notification = notificationCreatorService.createEventNotification(event);
        events.publishEvent(
                Notification.builder()
                        .type(Notification.NotificationType.EVENT)
                        .message(notification)
                        .build());
    }

    @Transactional
    public void deleteEventByName(String eventName) {
        List<Event> events = eventRepository.findAllByName(eventName);
        if (!events.isEmpty()) {
            eventRepository.deleteAll(events);
        }
    }

    @Transactional
    @Scheduled(cron = Constants.CRON_DELETE_EVENT, zone = Constants.TIME_ZONE)
    public void deleteEventByExpiredDate() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(Constants.TIME_ZONE));
        eventRepository.deleteByDateAndTimeBefore(now.toLocalDate(), now.toLocalTime());
    }

    public boolean existsEventByName(String name) {
        return eventRepository.existsByName(name);
    }
}
