package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.constants.CommandsConstants;
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
public class StopNotificationsCommand implements IBotCommand {
    private final SubscriberService subscriberService;

    private final TelegramMessageSender sender;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.STOP_NOTIFICATIONS;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.STOP_NOTIFICATIONS_DESCRIPTION;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command stop_notifications by userId={}, username={}", userId, user.getUserName());

        try {
            if (user.getIsBot()) {
                return;
            }

            Subscriber subscriber = subscriberService.findById(userId);
            if (subscriber.getGetEvents().equals(Boolean.FALSE)) {
                sender.sendMessageWithoutPhoto(chatId, CommandsConstants.STOP_NOTIFICATIONS_ALREADY_STOP_TEXT);
            } else {
                subscriber.setGetEvents(Boolean.FALSE);
                subscriberService.updateSubscriber(subscriber, userId);
                sender.sendMessageWithoutPhoto(chatId, CommandsConstants.STOP_NOTIFICATIONS_STOP_TEXT);
            }
        } catch (Exception e) {
            log.error("Error occurred in /stop_notifications command", e);
        }
    }
}