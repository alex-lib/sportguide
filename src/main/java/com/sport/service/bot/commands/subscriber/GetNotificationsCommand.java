package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.entities.Subscriber;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetNotificationsCommand implements IBotCommand {
    private final SubscriberService subscriberService;

    private final TelegramMessageSender sender;

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

        try {
            Subscriber subscriber = subscriberService.findById(userId);
            if (subscriber.getGetEvents().equals(Boolean.TRUE)) {
                sender.sendMessageWithoutPhoto(chatId, CommandsConstants.GET_NOTIFICATIONS_ALREADY_GET_TEXT);
            } else {
                subscriber.setGetEvents(Boolean.TRUE);
                subscriberService.updateSubscriber(subscriber, user.getId());
                sender.sendMessageWithoutPhoto(chatId, CommandsConstants.GET_NOTIFICATIONS_START_TEXT);
            }
        } catch (Exception e) {
            log.error("Error occurred in /get_notifications command", e);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
        }
    }
}