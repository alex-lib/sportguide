package com.sport.service.services.impl;

import com.sport.service.dto.EventDto;
import com.sport.service.entities.Event;
import com.sport.service.entities.subscriber.Subscriber;
import com.sport.service.events.EventCreatedEvent;
import com.sport.service.mappers.event.EventMapper;
import com.sport.service.repositories.EventRepository;
import com.sport.service.services.EventService;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@EnableScheduling
@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final SubscriberService subscriberService;

    private final EventMapper eventMapper;
    private final ApplicationEventPublisher eventPublisher;

    private static final String cron = "0 0 * * *";

    @Transactional
    @Override
    public void create(EventDto dto) {
        Event event = eventMapper.eventDtoToEvent(dto);
        eventRepository.save(event);
        List<Subscriber> subscribers = subscriberService.getSubscribersWhoWantGetEvents();
        eventPublisher.publishEvent(new EventCreatedEvent(subscribers, event));
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
    @Scheduled(cron = cron)
    @Override
    public void deleteByExpiredDate() {
        LocalDate currentDate = LocalDate.now();
        eventRepository.deleteByDateBefore(currentDate);
    }

    public boolean existsByName(String name) {
        return eventRepository.existsByName(name);
    }
}