package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.services.NotificationPreferenceService;
import com.sport.service.web.models.settings.NotificationPreferences;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@Slf4j
@RequiredArgsConstructor
public class StopNotificationsCommand implements IBotCommand {
    private final NotificationPreferenceService preferences;
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
        if (Boolean.TRUE.equals(message.getFrom().getIsBot())) return;
        try {
            preferences.update(message.getFrom().getId(), new NotificationPreferences(false, false, false, false, false));
            sender.sendMessageWithoutPhoto(message.getChatId(), CommandsConstants.STOP_NOTIFICATIONS_STOP_TEXT);
        } catch (Exception e) {
            log.error("Failed to disable notifications for user {}", message.getFrom().getId(), e);
            sender.sendMessageWithoutPhoto(message.getChatId(), ErrorConstants.ERROR_HAPPENED);
        }
    }
}
