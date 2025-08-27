package com.sport.service.services.impl;
import com.sport.service.entities.Event;
import com.sport.service.entities.subscriber.Subscriber;
import com.sport.service.events.EventCreatedEvent;
import com.sport.service.repositories.EventRepository;
import com.sport.service.services.EventService;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final SubscriberService subscriberService;

    private final ApplicationEventPublisher eventPublisher;

    private static final int CHECK_INTERVAL_DAYS = 1;

    @Transactional
    @Override
    public void create(Event event) {
        eventRepository.save(event);
        List<Subscriber> subscribers = subscriberService.getSubscribersWhoWantGetEvents();
        eventPublisher.publishEvent(new EventCreatedEvent(subscribers, event));
    }

    @Override
    public void deleteByName(String eventName) {
        Event event = eventRepository.findByName(eventName);
        if (event != null) {
            eventRepository.delete(event);
        }
    }

    @Override
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Scheduled(fixedRate = CHECK_INTERVAL_DAYS, timeUnit = TimeUnit.DAYS)
    @Override
    public void deleteByExpiredDate() {
        LocalDate currentDate = LocalDate.now();
        List<Event> list = eventRepository.findAll();
        for (Event event : list) {
            if (event.getDate().isBefore(currentDate)) {
                eventRepository.delete(event);
            }
        }
    }
}