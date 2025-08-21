package com.sport.service.services.impl;
import com.sport.service.entities.subscriber.RoleType;
import com.sport.service.entities.subscriber.Subscriber;
import com.sport.service.repositories.SubscriberRepository;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriberServiceImpl implements SubscriberService {

    private final SubscriberRepository subscriberRepository;

    @Override
    public void addSubscriber(User user) {
        Optional<Subscriber> subscriber = subscriberRepository.findById(user.getId());
        if (subscriber.isPresent()) updateSubscriber(user);
        if (subscriber.isEmpty()) {
            Subscriber transientSubscriber = Subscriber.builder()
                    .username(user.getUserName())
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();
            subscriberRepository.save(transientSubscriber);
            log.info("New user is saved - {}", user.getId());
        }
    }

    @Override
    public void updateSubscriber(User user) {
        Subscriber subscriber = Subscriber.builder()
                .username(user.getUserName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(RoleType.SUBSCRIBER)
                .build();
        log.info("User is found and updated - {}", user.getId());
        subscriberRepository.save(subscriber);
    }

    @Override
    public boolean checkIfAdmin(long userId) {
        Subscriber subscriber = subscriberRepository.findById(userId).orElse(null);
        return subscriber != null && subscriber.getRole() == RoleType.ADMIN;
    }

    @Override
    public Subscriber findById(long id) {
        return subscriberRepository.findById(id).orElse(null);
    }
}