package com.sport.service.bot.commands.admin;

import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.services.EventService;
import com.sport.service.services.SubscriberService;
import com.sport.service.sessions.CommandStateStore;
import com.sport.service.sessions.EventSession;
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

	private final EventSession eventSession;

	private final EventService eventService;

	private final CommandStateStore commandStateStore;

	private final SubscriberService subscriberService;

	@Override
	public String getCommandIdentifier() {
		return "delete_event";
	}

	@Override
	public String getDescription() {
		return "Let admin to delete a created event";
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		User user = message.getFrom();
		Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command delete_place by userId={}, username={}", userId, user.getUserName());
		SendMessage answer = new SendMessage();
		answer.setChatId(chatId);

        if (subscriberService.checkIfAdmin(userId)) {
            commandStateStore.setCurrentCommand(userId, "delete_event");
			answer.setText("Удалить событие можно только по точному имени ранее сохраненного события. " +
					"Напишите название события, которое хотите удалить:");
		}
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

        if (!"delete_event".equals(commandStateStore.getCurrentCommand(userId))) {
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
            errorMsg.setText("Ошибка при обработке ввода. Попробуйте еще раз \uD83D\uDD04");
			absSender.execute(errorMsg);
		} catch (Exception e) {
			log.error("Error sending error message", e);
		}
	}
}