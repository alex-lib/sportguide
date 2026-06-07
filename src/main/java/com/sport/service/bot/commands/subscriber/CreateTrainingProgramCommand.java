package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.services.ai.AiAssistantService;
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
public class CreateTrainingProgramCommand implements IBotCommand, TextProcessable {
    private final TelegramMessageSender sender;
    private final CommandStateStore commandStateStore;
    private final AiAssistantService aiAssistantService;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.CREATE_TRAINING_PROGRAM;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.CREATE_TRAINING_PROGRAM_DESCRIPTION;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        User user = message.getFrom();
        Long userId = user.getId();
        Long chatId = message.getChatId();
        log.info("Call command create_training_program by userId={}, username={}", userId, user.getUserName());

        commandStateStore.setCurrentCommand(userId, getCommandIdentifier());

        try {
            sender.sendMessageWithoutPhoto(chatId, CommandsConstants.CREATE_TRAINING_PROGRAM_START_TEXT);
        } catch (Exception e) {
            log.error("Error occurred in /create_training_program command", e);
            commandStateStore.clearCurrentCommand(userId);
        }
    }

    @Override
    public void processTextInput(AbsSender absSender, Message message) {
        Long userId = message.getFrom().getId();
        Long chatId = message.getChatId();

        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) return;

        String text = message.getText() == null ? "" : message.getText().trim();
        log.info("Received text for training program from user {}: {}", userId, text);

        if (text.equalsIgnoreCase(CommandsConstants.CREATE_TRAINING_PROGRAM_CANCEL)) {
            sender.sendMessageWithoutPhoto(chatId, CommandsConstants.CREATE_TRAINING_PROGRAM_CANCEL_TEXT);
            commandStateStore.clearCurrentCommand(userId);
            return;
        }

        try {
            sender.sendMessageWithoutPhoto(chatId, CommandsConstants.CREATE_TRAINING_PROGRAM_CREATING_TEXT);
            String trainingProgram = aiAssistantService.generateTrainingProgram(text);
            sender.sendMessageWithoutPhoto(chatId, trainingProgram);
        } catch (Exception e) {
            log.error("Error processing text input", e);
            sender.sendMessageWithoutPhoto(chatId, CommandsConstants.CREATE_TRAINING_PROGRAM_ERROR);
        } finally {
            commandStateStore.clearCurrentCommand(userId);
        }
    }
}