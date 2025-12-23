package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.services.NotificationCreatorService;
import com.sport.service.services.NotificationSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactAdminCommand implements IBotCommand, TextProcessable {
	private final NotificationCreatorService notificationCreatorService;
    private final NotificationSenderService notificationSenderService;

	private final CommandStateStore commandStateStore;

	private final TelegramMessageSender sender;

    @Value("${telegram.mainAdminId}")
    private String mainAdminId;

	@Override
	public String getCommandIdentifier() {
		return CommandsConstants.CONTACT_ADMIN;
	}

	@Override
	public String getDescription() {
		return CommandsConstants.CONTACT_ADMIN_DESCRIPTION;
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		User user = message.getFrom();
        Long userId = user.getId();
        Long chatId = message.getChatId();
        log.info("Call command contact_admin by userId={}, username={}", userId, user.getUserName());

		commandStateStore.setCurrentCommand(userId, getCommandIdentifier());

		try {
			sender.sendMessageWithoutPhoto(chatId, CommandsConstants.ENTER_TEXT);
		} catch (Exception e) {
			log.error("Error occurred in /contact_admin command", e);
			commandStateStore.clearCurrentCommand(userId);
		}
	}

    @Override
	public void processTextInput(AbsSender absSender, Message message) {
		User user = message.getFrom();
		Long userId = message.getFrom().getId();
		Long chatId = message.getChatId();

		if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		String text = message.getText();
		log.info("Received text: {}", text);

		try {
			String notification = notificationCreatorService.createSubscriberSentMessageToAdminNotification(text, user);
			notificationSenderService.sendSubscriberToAdminNotification(notification, Long.valueOf(mainAdminId));
			commandStateStore.clearCurrentCommand(userId);
			sender.sendMessageWithoutPhoto(chatId, CommandsConstants.MESSAGE_SENT_TO_ADMIN);
		} catch (Exception e) {
			log.error("Error processing text input", e);
			commandStateStore.clearCurrentCommand(userId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
		}
	}
}