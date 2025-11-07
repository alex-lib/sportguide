package com.sport.service.bot.commands.admin;

import com.sport.service.aop.annotations.AdminOnly;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.redis_store.commands_store.sessions.EventSession;
import com.sport.service.services.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@RequiredArgsConstructor
@Service
@Slf4j
public class DeleteEventCommand implements IBotCommand, TextProcessable {
	private final CommandStateStore commandStateStore;
	private final EventSession eventSession;

	private final EventService eventService;

	@Override
	public String getCommandIdentifier() {
		return "delete_event";
	}

	@Override
	public String getDescription() {
		return "Let admin to delete a created event";
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
        answer.setText("Удалить событие можно только по точному имени ранее сохраненного события. " +
                "Напишите название события, которое хотите удалить:");
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
		eventService.deleteByName(text);
		eventSession.clear(chatId);
		commandStateStore.clearCurrentCommand(userId);
        answer.setText("Удаление события завершено ✅");
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