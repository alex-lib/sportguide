package com.sport.service.bot.commands.admin;

import com.sport.service.annotations.AdminOnly;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.entities.enums.event.CreateEventStep;
import com.sport.service.entities.enums.event.EventState;
import com.sport.service.store.commands.CommandStateStore;
import com.sport.service.store.commands.sessions.EventSession;
import com.sport.service.services.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Slf4j
@RequiredArgsConstructor
@Service
public class CreateEventCommand implements IBotCommand, TextProcessable, CallbackProcessable {
    private final EventSession eventSession;
    private final EventService eventService;
    private final CommandStateStore commandStateStore;
    private final TelegramMessageSender sender;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.CREATE_EVENT;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.CREATE_EVENT_DESCRIPTION;
    }

    @AdminOnly
    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long userId = user.getId();
        Long chatId = message.getChatId();
        log.info("Call command create_event by userId={}, username={}", userId, user.getUserName());

        try {
            EventState state = eventSession.createSession(chatId);
            commandStateStore.setCurrentCommand(userId, getCommandIdentifier());

            SendMessage answer = new SendMessage();
            answer.setChatId(chatId.toString());
            answer.setText(CommandsConstants.CREATING_TYPE);

            InlineKeyboardMarkup keyboard = CreateEventStep.DISTRICT.buildKeyboard(answer, state);
            answer.setReplyMarkup(keyboard);

            sender.sendMessageWithoutPhoto(answer);
        } catch (Exception e) {
            log.error("Error to start processing message", e);
            eventSession.clear(chatId);
            commandStateStore.clearCurrentCommand(userId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
        }
    }

    @Override
    public void processCallback(AbsSender absSender, CallbackQuery callback) {
        Long chatId = callback.getMessage().getChatId();
        Long userId = callback.getFrom().getId();

        EventState state = eventSession.getIfExists(chatId);
        if (!ifSessionValid(chatId, state, userId)) {
            return;
        }

        String data = callback.getData();
        log.info("Processing callback for create_event: step={}, data={}", state.getStep(), data);

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        try {
            if (state.getStep().isCallbackStep()) {
                CreateEventStep nextStep = state.getStep().onCallback(data, state, eventService);
                state.setStep(nextStep);

                InlineKeyboardMarkup keyboard = nextStep.buildKeyboard(answer, state);
                answer.setReplyMarkup(keyboard);

                if (nextStep.isFinished()) {
                    eventService.createEvent(state);
                    answer.setText(CommandsConstants.EVENT_CREATED);
                    eventSession.clear(chatId);
                    commandStateStore.clearCurrentCommand(userId);
                }
            }

            eventSession.save(chatId, state);
            sender.sendMessageWithoutPhoto(answer);
        } catch (Exception e) {
            log.error("Error processing callback", e);
            eventSession.clear(chatId);
            commandStateStore.clearCurrentCommand(userId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
        }
    }

    @Override
    public void processTextInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        EventState state = eventSession.getIfExists(chatId);
        if (!ifSessionValid(chatId, state, userId)) {
            return;
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        String text = message.getText();

        try {
            if (state.getStep().isTextStep()) {
                String nextStepName = state.getStep().handleText(answer, text, state, eventService);

                if (nextStepName != null) {
                    CreateEventStep nextStep = CreateEventStep.valueOf(nextStepName);
                    state.setStep(nextStep);

                    InlineKeyboardMarkup keyboard = nextStep.buildKeyboard(answer, state);
                    answer.setReplyMarkup(keyboard);
                }

                eventSession.save(chatId, state);

                if (state.getStep().isFinished()) {
                    eventService.createEvent(state);
                    answer.setText(CommandsConstants.EVENT_CREATED);
                    eventSession.clear(chatId);
                    commandStateStore.clearCurrentCommand(userId);
                    sender.sendMessageWithoutPhoto(answer);
                    log.info("Event created successfully by userId={}, eventName={}", userId, state.getName());
                } else if (!answer.getText().isEmpty()) {
                    sender.sendMessageWithoutPhoto(answer);
                }
            } else {
                handleUnknownStep(chatId, userId, answer);
                sender.sendMessageWithoutPhoto(answer);
            }
        } catch (Exception e) {
            log.error("Error processing text input", e);
            eventSession.clear(chatId);
            commandStateStore.clearCurrentCommand(userId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
        }
    }

    private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
        answer.setText(ErrorConstants.UNKNOWN_STEP);
        eventSession.clear(chatId);
        commandStateStore.clearCurrentCommand(userId);
    }

    private boolean ifSessionValid(Long chatId, EventState state, Long userId) {
        if (state == null) {
            log.warn("No session found for chatId: {}", chatId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
            return false;
        }

        String currentCommand = commandStateStore.getCurrentCommand(userId);
        if (!getCommandIdentifier().equals(currentCommand)) {
            log.warn("User {} is not in create_event session", chatId);
            return false;
        }

        return true;
    }
}
