package com.sport.service.services;

import com.sport.service.constants.Constants;
import com.sport.service.dto.EventDto;
import com.sport.service.entities.Event;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.common.District;
import com.sport.service.mappers.event.EventMapper;
import com.sport.service.repositories.EventRepository;
import com.sport.service.web.models.event.ListEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final SubscriberService subscriberService;
    private final NotificationCreatorService notificationCreatorService;
    private final NotificationSenderService notificationSenderService;
    private final EventMapper eventMapper;

    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }

    public ListEventResponse findAllEventsWithFilter(String districtStr, String dateStr, String search) {
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
        List<Long> subscriberIds = subscriberService.getSubscribersWhoWantGetEvents()
                .stream()
                .map(Subscriber::getId)
                .toList();
        String notification = notificationCreatorService.createEventNotification(event);
        notificationSenderService.sendEventNotification(notification, subscriberIds);
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
