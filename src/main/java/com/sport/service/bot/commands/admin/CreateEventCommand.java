package com.sport.service.bot.commands.admin;

import com.sport.service.aop.annotations.AdminOnly;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.dto.EventDto;
import com.sport.service.entities.enums.common.District;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.redis_store.commands_store.sessions.EventSession;
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

@RequiredArgsConstructor
@Service
@Slf4j
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
            EventDto dto = eventSession.createSession(chatId);
            dto.setStep(1);
            eventSession.save(chatId, dto);
            commandStateStore.setCurrentCommand(userId, getCommandIdentifier());

            SendMessage answer = new SendMessage();
            answer.setChatId(chatId.toString());
            answer.setText(CommandsConstants.CREATING_TYPE);
            answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboard(answer));

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

        EventDto dto = eventSession.getIfExists(chatId);
        if (!ifSessionValid(chatId, dto)) {
            return;
        }

        String data = callback.getData();
        log.info("Processing callback for create_event: step={}, data={}", dto.getStep(), data);

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        try {
            if (dto.getStep() == 1) {
                handleDistrictStep(dto, data, answer);
            } else {
                handleUnknownStep(chatId, userId, answer);
            }
            eventSession.save(chatId, dto);
            sender.sendMessageWithoutPhoto(answer);
        } catch (Exception e) {
            log.error("Error processing callback", e);
            eventSession.clear(chatId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
        }
    }

    private void handleDistrictStep(EventDto dto, String data, SendMessage answer) {
        dto.setDistrict(District.valueOf(data));
        answer.setText(CommandsConstants.ENTER_EVENT_NAME);
        dto.setStep(2);
    }

    @Override
    public void processTextInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        EventDto dto = eventSession.getIfExists(chatId);
        if (!ifSessionValid(chatId, dto)) {
            return;
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        try {
            handleTextInput(message, dto, answer);
            eventSession.save(chatId, dto);
            absSender.execute(answer);

            if (dto.getStep() > 8) {
                eventService.create(dto);
                sender.sendMessageWithoutPhoto(chatId, CommandsConstants.EVENT_CREATED);
                log.info("Event created successfully by userId={}, eventName={}", userId, dto.getName());
                eventSession.clear(chatId);
                commandStateStore.clearCurrentCommand(userId);
            }
        } catch (Exception e) {
            log.error("Error processing text input", e);
            eventSession.clear(chatId);
            commandStateStore.clearCurrentCommand(userId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
        }
    }

    private void handleTextInput(Message message, EventDto dto, SendMessage answer) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        String text = message.getText();

        switch (dto.getStep()) {
            case 2 -> {
                if (!eventService.existsByName(text)) {
                    dto.setName(text);
                    answer.setText(CommandsConstants.ENTER_EVENT_ADDRESS);
                    dto.setStep(3);
                } else {
                    answer.setText(CommandsConstants.EVENT_NAME_IS_EXISTED);
                }
            }
            case 3 -> {
                dto.setAddress(text);
                answer.setText(CommandsConstants.ENTER_EVENT_DESCRIPTION);
                dto.setStep(4);
            }
            case 4 -> {
                dto.setDescription(text);
                answer.setText(CommandsConstants.ENTER_EVENT_LINK);
                dto.setStep(5);
            }
            case 5 -> {
                dto.setLink(text);
                answer.setText(CommandsConstants.ENTER_EVENT_PLACE);
                dto.setStep(6);
            }
            case 6 -> {
                dto.setPlaceName(text);
                answer.setText(CommandsConstants.ENTER_EVENT_DATE);
                dto.setStep(7);
            }
            case 7 -> {
                if (isValidDate(text)) {
                    dto.setDate(text);
                    answer.setText(CommandsConstants.ENTER_EVENT_TIME);
                    dto.setStep(8);
                } else {
                    answer.setText(CommandsConstants.EVENT_DATE_IS_INVALID);
                }
            }
            case 8 -> {
                if (isValidTime(text)) {
                    dto.setTime(text);
                    answer.setText(CommandsConstants.DATA_IS_RECEIVED);
                    dto.setStep(9);
                } else {
                    answer.setText(CommandsConstants.EVENT_TIME_IS_INVALID);
                }
            }
            default -> handleUnknownStep(chatId, userId, answer);
        }
    }

    private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
        answer.setText(ErrorConstants.UNKNOWN_STEP);
        eventSession.clear(chatId);
        commandStateStore.clearCurrentCommand(userId);
    }

    private boolean isValidDate(String date) {
        return CommandsConstants.DATE_PATTERN.matcher(date).matches();
    }

    private boolean isValidTime(String time) {
        return CommandsConstants.TIME_PATTERN.matcher(time).matches();
    }

    private boolean ifSessionValid(Long chatId, EventDto dto) {
        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(chatId))) {
            log.warn("User {} is not in create_event session", chatId);
            return false;
        }

        if (dto == null) {
            log.warn("No session found for chatId: {}", chatId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
            commandStateStore.clearCurrentCommand(chatId);
            return false;
        }
        return true;
    }
}