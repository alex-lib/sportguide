package com.sport.service.bot.commands.admin;

import com.sport.service.events.EventSendMessageToAllUsers;
import com.sport.service.services.SubscriberService;
import com.sport.service.sessions.CommandStateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendMessageToAllUsersCommand implements IBotCommand {

    private final ApplicationEventPublisher eventPublisher;

    private final CommandStateStore commandStateStore;

    private final SubscriberService subscriberService;

    @Override
    public String getCommandIdentifier() {
        return "send_message_to_all_users";
    }

    @Override
    public String getDescription() {
        return "Let admin send message to all users";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command send_message_to_all_users by userId={}, username={}", userId, user.getUserName());
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId);

        if (subscriberService.checkIfAdmin(userId)) {
            commandStateStore.setCurrentCommand(userId, "send_message_to_all_users");
            answer.setText("📩 Напишите, что вы хотите отправить подисчикам:");
        }
        try {
            answer.setChatId(chatId.toString());
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /send_message_to_all_users command", e);
        }
    }

    public void processTextInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        if (!"send_message_to_all_users".equals(commandStateStore.getCurrentCommand(userId))) {
            return;
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());
        String text = message.getText();
        log.info("Received text: {}", text);
        eventPublisher.publishEvent(new EventSendMessageToAllUsers(text, subscriberService.findAll()));
        commandStateStore.clearCurrentCommand(userId);
        answer.setText("Сообщение отправлено всем пользователям ✅");
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
            errorMsg.setText("Ошибка при обработке ввода. Попробуйте еще раз \uD83D\uDD04");
            absSender.execute(errorMsg);
        } catch (Exception e) {
            log.error("Error sending error message", e);
        }
    }
}