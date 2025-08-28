package com.sport.service.services.impl;
import com.sport.service.entities.subscriber.RoleType;
import com.sport.service.entities.subscriber.Subscriber;
import com.sport.service.repositories.SubscriberRepository;
import com.sport.service.services.SubscriberService;
import com.sport.service.utils.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriberServiceImpl implements SubscriberService {

    private final SubscriberRepository subscriberRepository;

    @Override
    public void addSubscriber(User user) {
        Optional<Subscriber> subscriber = subscriberRepository.findById(user.getId());

        if (subscriber.isPresent()) {
            Subscriber updatedSubscriber = Subscriber.builder()
                    .username(user.getUserName())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();
            updateSubscriber(updatedSubscriber, user.getId());
        }

        if (subscriber.isEmpty()) {
            Subscriber transientSubscriber = Subscriber.builder()
                    .username(user.getUserName())
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(RoleType.SUBSCRIBER)
                    .getEvents(true)
                    .build();
            subscriberRepository.save(transientSubscriber);
            log.info("New user is saved - {}", user.getId());
        }
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

    @Override
    public List<Subscriber> getSubscribersWhoWantGetEvents() {
        return subscriberRepository.findAll().stream().filter(subscriber -> subscriber.getGetEvents().equals(true)).toList();
    }

    @Override
    public void updateSubscriber(Subscriber subscriber, Long id) {
        Subscriber existedSubscriber = findById(id);
        BeanUtils.copyNonNullProperties(subscriber, existedSubscriber);
        subscriberRepository.save(existedSubscriber);
    }

    @Override
    public int getUsersCount() {
        return subscriberRepository.getUsersCount();
    }

    @Override
    public int getSubscriptionsCount() {
        return subscriberRepository.getSubscriptionsCount();
    }
}