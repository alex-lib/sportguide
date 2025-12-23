package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.entities.Event;
import com.sport.service.services.EventService;
import com.sport.service.services.NotificationCreatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetUpcomingEventsCommand implements IBotCommand {
    private final EventService eventService;
    private final NotificationCreatorService notificationCreatorService;

    private final TelegramMessageSender sender;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.GET_UPCOMING_EVENTS;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.GET_UPCOMING_EVENTS_DESCRIPTION;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long userId = user.getId();
        log.info("Call command get_upcoming_events by userId={}, username={}", userId, user.getUserName());

        try {
            List<Event> events = eventService.findAll();
            if (events.isEmpty()) {
                sender.sendMessageWithoutPhoto(userId,
                        CommandsConstants.THERE_ARE_NO_EVENTS);
            } else {
                for (Event event : events) {
                    sender.sendMessageWithoutPhoto(userId,
                            notificationCreatorService.createEventNotification(event));
                }
            }
        } catch (Exception e) {
            log.error("Error occurred in /get_upcoming_events command", e);
            sender.sendMessageWithoutPhoto(userId, ErrorConstants.ERROR_HAPPENED);
        }
    }
}