package com.sport.service.services;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.subscriber.RoleType;
import com.sport.service.repositories.SubscriberRepository;
import com.sport.service.utils.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;
    private final TelegramMessageSender sender;
    private final NotificationSenderService notificationSenderService;
    private final NotificationCreatorService notificationCreatorService;

    @Value("${telegram.mainAdminId}")
    private String adminId;

    @Transactional
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

            sendNewUserAlert(transientSubscriber);

            String greetingMessage = user.getUserName() != null ?
                    "<i>Привет</i> @" + user.getUserName() + " \uD83D\uDC4B" + "\n" + CommandsConstants.GREETING_MESSAGE :
                    "<i>Привет</i> \uD83D\uDC4B" + "\n" + CommandsConstants.GREETING_MESSAGE;

            sender.sendMessageWithoutPhoto(user.getId(), greetingMessage);
        }
    }

    public boolean checkIfAdmin(long userId) {
        Subscriber subscriber = subscriberRepository.findById(userId).orElse(null);
        return subscriber != null && subscriber.getRole() == RoleType.ADMIN;
    }

    public Subscriber findById(long id) {
        return subscriberRepository.findById(id).orElse(null);
    }

    public List<Subscriber> getSubscribersWhoWantGetEvents() {
        return subscriberRepository.findAllByGetEventsTrue();
    }

    @Transactional
    public void updateSubscriber(Subscriber subscriber, Long id) {
        Subscriber existedSubscriber = findById(id);
        BeanUtils.copyNonNullProperties(subscriber, existedSubscriber);
        subscriberRepository.save(existedSubscriber);
    }

    public long getUsersCount() {
        return subscriberRepository.count();
    }

    public long getSubscriptionsCount() {
        return subscriberRepository.countByGetEventsTrue();
    }

    public List<Subscriber> findAll() {
        return subscriberRepository.findAll();
    }

    private void sendNewUserAlert(Subscriber subscriber) {
        String message = notificationCreatorService.createNewUserAlert(subscriber);
        notificationSenderService.sendAdminAlertNotification(message, Long.valueOf(adminId));
    }
}