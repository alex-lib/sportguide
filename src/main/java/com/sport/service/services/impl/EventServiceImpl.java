package com.sport.service.services.impl;

import com.sport.service.dto.EventDto;
import com.sport.service.entities.Event;
import com.sport.service.entities.Subscriber;
import com.sport.service.mappers.event.EventMapper;
import com.sport.service.repositories.EventRepository;
import com.sport.service.services.EventService;
import com.sport.service.services.NotificationCreatorService;
import com.sport.service.services.NotificationSenderService;
import com.sport.service.services.SubscriberService;
import com.sport.service.specifications.EventSpecification;
import com.sport.service.web.models.event.EventFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;

    private final SubscriberService subscriberService;
    private final NotificationCreatorService notificationCreatorService;
    private final NotificationSenderService notificationSenderService;

    private final EventMapper eventMapper;

    private static final String CRON = "0 0 0 * * *";

    @Transactional
    @Override
    public void create(EventDto dto) {
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
    @Override
    public void deleteByName(String eventName) {
        List<Event> events = eventRepository.findAllByName(eventName);
        if (!events.isEmpty()) {
            eventRepository.deleteAll(events);
        }
    }

    @Override
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Transactional
    @Scheduled(cron = CRON, zone = "Europe/Moscow")
    @Override
    public void deleteByExpiredDate() {
        LocalDate currentDate = LocalDate.now();
        eventRepository.deleteByDateBefore(currentDate);
    }

    @Override
    public boolean existsByName(String name) {
        return eventRepository.existsByName(name);
    }

    @Override
    public List<Event> findAll(EventFilter filter) {
        return eventRepository.findAll(EventSpecification.withFilter(filter));
    }
}