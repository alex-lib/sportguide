package com.sport.service.services;
import com.sport.service.entities.subscriber.Subscriber;
import org.telegram.telegrambots.meta.api.objects.User;

public interface SubscriberService {

    void addSubscriber(User user);

    void updateSubscriber(User user);

    boolean checkIfAdmin(long userId);

    Subscriber findById(long id);
}