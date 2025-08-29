package com.sport.service.bot.commands.admin;
import com.sport.service.services.SubscriberService;
import com.sport.service.sessions.PlaceSession;
import com.sport.service.services.PlaceService;
import com.sport.service.sessions.CommandStateStore;
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
public class DeletePlaceCommand implements IBotCommand {

	private final PlaceService placeService;

	private final PlaceSession placeSession;

	private final CommandStateStore commandStateStore;

	private final SubscriberService subscriberService;

	@Override
	public String getCommandIdentifier() {
		return "delete_place";
	}

	@Override
	public String getDescription() {
		return "Let admin to delete a created place";
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		User user = message.getFrom();
		log.info("Call command delete_place by user: {}", user.getUserName());
		Long chatId = message.getChatId();
		SendMessage answer = new SendMessage();
		answer.setChatId(chatId);

		if (subscriberService.checkIfAdmin(user.getId())) {
			commandStateStore.setCurrentCommand(user.getId(), "delete_place");
			answer.setText("Удалить место можно только по точному имени ранее сохраненного места. " +
					"Напишите название места, которое хотите удалить:");
			String text = message.getText();
			log.info("Received text: {}", text);
		} else {
			answer.setText("Вы не являетесь администратором.");
		}
		try {
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error sending initial message", e);
		}
	}

	public void processTextInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

		if (!"delete_place".equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());
		String text = message.getText();
		log.info("Received text: {}", text);
		placeService.deleteByName(text);
		placeSession.clear(chatId);
		commandStateStore.clearCurrentCommand(userId);
		answer.setText("Удаление места завершено.");

		try {
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error processing text input", e);
			sendErrorMessage(absSender, chatId, "Ошибка при обработке ввода. Попробуйте еще раз.");
		}
	}

	private void sendErrorMessage(AbsSender absSender, Long chatId, String message) {
		try {
			SendMessage errorMsg = new SendMessage();
			errorMsg.setChatId(chatId.toString());
			errorMsg.setText(message);
			absSender.execute(errorMsg);
		} catch (Exception e) {
			log.error("Error sending error message", e);
		}
	}
}