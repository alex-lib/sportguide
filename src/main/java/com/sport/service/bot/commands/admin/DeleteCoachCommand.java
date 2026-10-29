package com.sport.service.bot.commands.admin;

import com.sport.service.annotations.AdminOnly;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.services.CoachService;
import com.sport.service.store.commands.CommandStateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteCoachCommand implements IBotCommand, TextProcessable {
    private final CommandStateStore commandStateStore;
    private final TelegramMessageSender sender;
    private final CoachService coachService;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.DELETE_COACH;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.DELETE_COACH_DESCRIPTION;
    }

    @AdminOnly
    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Long userId = message.getFrom().getId();
        try {
            commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
            sender.sendMessageWithoutPhoto(message.getChatId(), CommandsConstants.DELETING_COACH_INSTRUCTION);
        } catch (Exception e) {
            log.error("Failed to start delete_coach for user {}", userId, e);
            commandStateStore.clearCurrentCommand(userId);
            sender.sendMessageWithoutPhoto(message.getChatId(), ErrorConstants.ERROR_HAPPENED);
        }
    }

    @AdminOnly
    @Override
    public void processTextInput(AbsSender absSender, Message message) {
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();
        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) return;

        String username = message.getText().trim();
        if ("/cancel".equals(username)) {
            commandStateStore.clearCurrentCommand(userId);
            sender.sendMessageWithoutPhoto(chatId, CommandsConstants.DELETE_COACH_CANCELLED);
            return;
        }

        try {
            if (!coachService.deleteCoachByUsername(username)) {
                commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
                sender.sendMessageWithoutPhoto(chatId, CommandsConstants.COACH_NOT_FOUND_FOR_DELETION);
                return;
            }
        } catch (IllegalArgumentException e) {
            commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
            sender.sendMessageWithoutPhoto(chatId, e.getMessage());
            return;
        } catch (Exception e) {
            log.error("Failed to delete coach for user {}", userId, e);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
            return;
        }
        commandStateStore.clearCurrentCommand(userId);
        sender.sendMessageWithoutPhoto(chatId, CommandsConstants.COACH_DELETED);
    }
}
