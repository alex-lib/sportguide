package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.services.NotificationCreatorService;
import com.sport.service.services.NotificationSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactAdminCommand implements IBotCommand, TextProcessable {
	private final NotificationCreatorService notificationCreatorService;
    private final NotificationSenderService notificationSenderService;

	private final CommandStateStore commandStateStore;

    @Value("${telegram.mainAdminId}")
    private String mainAdminId;

	@Override
	public String getCommandIdentifier() {
		return "contact_admin";
	}

	@Override
	public String getDescription() {
		return "Let subscriber to contact admin";
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		User user = message.getFrom();
        Long userId = user.getId();
        Long chatId = message.getChatId();
        log.info("Call command contact_admin by userId={}, username={}", userId, user.getUserName());
		SendMessage answer = new SendMessage();
        answer.setChatId(chatId);

		commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
        answer.setText("📩 Напишите ваше предложение (ТОЛЬКО ТЕКСТ):");
		try {
			absSender.execute(answer);
		} catch (TelegramApiException e) {
			log.error("Error occurred in /contact_admin command", e);
		}
	}

    @Override
	public void processTextInput(AbsSender absSender, Message message) {
		Long userId = message.getFrom().getId();

		if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		Long chatId = message.getChatId();
		User user = message.getFrom();
		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());

		String text = message.getText();
		log.info("Received text: {}", text);

		String notification = notificationCreatorService.createSubscriberSentMessageToAdminNotification(text, user);
        notificationSenderService.sendSubscriberToAdminNotification(notification, Long.valueOf(mainAdminId));
		commandStateStore.clearCurrentCommand(userId);
        answer.setText("Сообщение отправлено админу ✅");
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