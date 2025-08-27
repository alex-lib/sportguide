package com.sport.service.events;
import com.sport.service.entities.Event;
import com.sport.service.entities.subscriber.Subscriber;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class EventCreatedEvent {

    private final List<Subscriber> subscribers;
    private final Event event;
}