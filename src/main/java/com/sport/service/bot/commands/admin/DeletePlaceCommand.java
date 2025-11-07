package com.sport.service.bot.commands.admin;

import com.sport.service.aop.annotations.AdminOnly;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.redis_store.commands_store.sessions.PlaceSession;
import com.sport.service.services.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeletePlaceCommand implements IBotCommand, TextProcessable {
    private final PlaceSession placeSession;
    private final CommandStateStore commandStateStore;

    private final PlaceService placeService;

    @Override
    public String getCommandIdentifier() {
        return "delete_place";
    }

    @Override
    public String getDescription() {
        return "Let admin to delete a created place";
    }

    @Override
    @AdminOnly
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command delete_place by userId={}, username={}", userId, user.getUserName());
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId);

        commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
        answer.setText("Удалить место можно только по точному имени ранее сохраненного места. " +
                "Напишите название места, которое хотите удалить:");
        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error sending initial message", e);
        }
    }

    @Override
    public void processTextInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) {
            return;
        }

        String text = message.getText();
        log.info("Received text: {}", text);
        placeService.deleteByName(text);
        placeSession.clear(chatId);
        commandStateStore.clearCurrentCommand(userId);
        answer.setText("Удаление места завершено ✅");
        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error processing text input", e);
            sendErrorMessage(absSender, chatId);
        }
    }

    private void sendErrorMessage(AbsSender absSender, Long chatId) {
        try {
            SendMessage errorMsg = new SendMessage();
            errorMsg.setChatId(chatId.toString());
            errorMsg.setText(ErrorConstants.ENTERING_ERROR);
            absSender.execute(errorMsg);
        } catch (Exception e) {
            log.error("Error sending error message", e);
        }
    }
}