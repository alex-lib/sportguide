package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.entities.Subscriber;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetNotificationsCommand implements IBotCommand {
    private final SubscriberService subscriberService;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.GET_NOTIFICATIONS;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.GET_NOTIFICATIONS_DESCRIPTION;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long userId = user.getId();
        Long chatId = message.getChatId();
        log.info("Call command get_notifications by userId={}, username={}", userId, user.getUserName());
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        Subscriber subscriber = subscriberService.findById(userId);
        if (subscriber.getGetEvents().equals(Boolean.TRUE)) {
            answer.setText(CommandsConstants.GET_NOTIFICATIONS_ALREADY_GET_TEXT);
        } else {
            subscriber.setGetEvents(Boolean.TRUE);
            subscriberService.updateSubscriber(subscriber, user.getId());
            answer.setText(CommandsConstants.GET_NOTIFICATIONS_START_TEXT);
        }
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /get_upcoming_events command", e);
        }
    }
}