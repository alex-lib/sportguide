package com.sport.service.bot.commands.admin;

import com.sport.service.annotations.AdminOnly;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetSubscriptionsCountCommand implements IBotCommand {
    private final SubscriberService subscriberService;

    private final TelegramMessageSender sender;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.GET_SUBSCRIPTIONS_COUNT;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.GET_SUBSCRIPTIONS_COUNT_DESCRIPTION;
    }

    @AdminOnly
    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command get_subscriptions_count by userId={}, username={}", userId, user.getUserName());

        try {
            String answer = CommandsConstants.GET_SUBSCRIPTIONS_COUNT_TEXT + subscriberService.getSubscriptionsCount();
            sender.sendMessageWithoutPhoto(chatId, answer);
        } catch (Exception e) {
            log.error("Error occurred in /get_subscriptions_count command", e);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
        }
    }
}