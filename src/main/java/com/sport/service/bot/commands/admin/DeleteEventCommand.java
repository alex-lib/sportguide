package com.sport.service.bot.commands.admin;

import com.sport.service.aop.annotations.AdminOnly;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.services.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@RequiredArgsConstructor
@Service
@Slf4j
public class DeleteEventCommand implements IBotCommand, TextProcessable {
	private final CommandStateStore commandStateStore;

	private final TelegramMessageSender sender;

	private final EventService eventService;

	@Override
	public String getCommandIdentifier() {
        return CommandsConstants.DELETE_EVENT;
	}

	@Override
	public String getDescription() {
        return CommandsConstants.DELETE_EVENT_DESCRIPTION;
	}

	@AdminOnly
	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		User user = message.getFrom();
		Long chatId = message.getChatId();
        Long userId = user.getId();

        log.info("Call command delete_place by userId={}, username={}", userId, user.getUserName());

		try {
			commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
			sender.sendMessageWithoutPhoto(chatId, CommandsConstants.DELETING_EVENT_INSTRUCTION);
		} catch (Exception e) {
			log.error("Error to start processing message", e);
			commandStateStore.clearCurrentCommand(userId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
		}
	}

	@Override
	public void processTextInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) {
            return;
        }

		try {
			String text = message.getText();
			log.info("Received text: {}", text);
			eventService.deleteEventByName(text);
			commandStateStore.clearCurrentCommand(userId);
			sender.sendMessageWithoutPhoto(chatId, CommandsConstants.EVENT_DELETED);
		} catch (Exception e) {
			log.error("Error processing text input", e);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
		}
	}
}