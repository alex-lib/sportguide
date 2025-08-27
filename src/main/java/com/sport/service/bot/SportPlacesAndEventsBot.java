package com.sport.service.bot;
import com.sport.service.bot.commands.admin.CreateEventCommand;
import com.sport.service.bot.commands.admin.DeleteEventCommand;
import com.sport.service.bot.commands.admin.DeletePlaceCommand;
import com.sport.service.bot.commands.subscriber.ContactAdminCommand;
import com.sport.service.bot.commands.subscriber.GetPlaceCommand;
import com.sport.service.bot.commands.admin.CreatePlaceCommand;
import com.sport.service.sessions.CommandStateStore;
import com.sport.service.entities.Event;
import com.sport.service.entities.subscriber.Subscriber;
import com.sport.service.events.EventContactAdmin;
import com.sport.service.events.EventCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;

@Service
@Slf4j
public class SportPlacesAndEventsBot extends TelegramLongPollingCommandBot {

	private final CommandStateStore commandStateStore;
	private final String botUsername;
	private final CreatePlaceCommand createPlaceCommand;
	private final GetPlaceCommand getPlaceCommand;
	private final DeletePlaceCommand deletePlaceCommand;
	private final CreateEventCommand createEventCommand;
	private final DeleteEventCommand deleteEventCommand;
	private final ContactAdminCommand contactAdminCommand;

	public SportPlacesAndEventsBot(
			@Value("${telegram.bot.token}") String botToken,
			@Value("${telegram.bot.username}") String botUsername,
			List<IBotCommand> commandList,
			CreatePlaceCommand createPlaceCommand,
			GetPlaceCommand getPlaceCommand,
			DeletePlaceCommand deletePlaceCommand,
			CreateEventCommand createEventCommand,
			DeleteEventCommand deleteEventCommand,
			ContactAdminCommand contactAdminCommand,
			CommandStateStore commandStateStore) {
		super(botToken);
		this.botUsername = botUsername;
		this.createPlaceCommand = createPlaceCommand;
		this.getPlaceCommand = getPlaceCommand;
		this.deletePlaceCommand = deletePlaceCommand;
		this.createEventCommand = createEventCommand;
		this.deleteEventCommand = deleteEventCommand;
		this.contactAdminCommand = contactAdminCommand;
		this.commandStateStore = commandStateStore;
		commandList.forEach(this::register);
	}

	@Value("${telegram.mainAdminId}")
	private String mainAdminId;

	@Override
	public String getBotUsername() {
		return botUsername;
	}

	@Override
	public void processNonCommandUpdate(Update update) {

		if (update.hasCallbackQuery()) {
			CallbackQuery callback = update.getCallbackQuery();
			if (callback == null || callback.getFrom() == null) return;

			// 🚀 Ignore bot’s own callbacks
			if (callback.getFrom().getIsBot()) return;

			long userId = callback.getFrom().getId();
			log.info("Callback from user: {}", userId);
			String data = callback.getData();
			log.info("Callback data: {}", data);

			if (!(callback.getMessage() instanceof Message)) return;
			Message message = (Message) callback.getMessage();

			String currentCommand = commandStateStore.getCurrentCommand(userId);
			log.info("currentCommand " + currentCommand);

			if ("get_place".equals(currentCommand)) {
				commandStateStore.addSelection(userId, data);
				List<String> args = commandStateStore.getSelections(userId);
				getPlaceCommand.processMessage(this, message, args.toArray(new String[0]));
				if (args.size() >= 3) {
					commandStateStore.clearSelections(userId);
					commandStateStore.clearCurrentCommand(userId);
				}
			} else if ("create_place".equals(currentCommand)) {
				createPlaceCommand.processCallback(this, callback);
				return;
			} else if ("create_event".equals(currentCommand)) {
				createEventCommand.processCallback(this, callback);
				return;
			}
			return;
		}

		if (update.hasMessage() && update.getMessage().hasText()) {
			Message message = update.getMessage();
			long userId = message.getFrom().getId();
			String currentCommand = commandStateStore.getCurrentCommand(userId);

			if ("create_place".equals(currentCommand)) {
				createPlaceCommand.processTextInput(this, message);
				return;
			}

			if ("delete_place".equals(currentCommand)) {
				deletePlaceCommand.processTextInput(this, message);
				return;
			}

			if ("create_event".equals(currentCommand)) {
				createEventCommand.processTextInput(this, message);
				return;
			}

			if ("delete_event".equals(currentCommand)) {
				deleteEventCommand.processTextInput(this, message);
				return;
			}

			if ("contact_admin".equals(currentCommand)) {
				contactAdminCommand.processTextInput(this, message);
				return;
			}
		}

		if (update.hasMessage() && update.getMessage().hasPhoto()) {
			Message message = update.getMessage();
			long userId = message.getFrom().getId();
			String currentCommand = commandStateStore.getCurrentCommand(userId);

			if ("create_place".equals(currentCommand)) {
				createPlaceCommand.processPhotoInput(this, message);
			}
		}
	}

	@EventListener
	private void sendNotification(EventCreatedEvent event) {
		try {
			String notification = createEventNotification(event.getEvent());
			for (Subscriber subscriber : event.getSubscribers()) {
				SendMessage sendMessage = SendMessage.builder()
						.chatId(subscriber.getId().toString())
						.text(notification)
						.build();
				execute(sendMessage);
			}
		} catch (TelegramApiException e) {
			log.error("Failed to send notification of {}", event.getEvent());
		}
	}

	private String createEventNotification(Event event) {
		return new StringBuilder()
				.append("Событие: ").append(event.getName()).append("\n")
				.append("Описание: ").append(event.getDescription()).append("\n")
				.append("Дата: ").append(event.getDate()).append("\n")
				.append("Время: ").append(event.getTime()).append("\n")
				.append("Место: ").append(event.getPlaceName()).append("\n")
				.append("Ссылка: ").append(event.getLink()).append("\n")
				.append("Район: ").append(event.getDistrict()).append("\n")
				.append("Адрес: ").append(event.getAddress()).append("\n")
				.append("Спасибо за подписку!")
				.toString();
	}

	@EventListener
	private void sendNotificationToAdmin(EventContactAdmin event) {
		try {
			String notification = createNotificationForAdmin(event.getText(), event.getUser());
			SendMessage sendMessage = SendMessage.builder()
					.chatId(mainAdminId)
					.text(notification)
					.build();
			execute(sendMessage);
		} catch (TelegramApiException e) {
			log.error("Failed to send notification of {}", event.getText());
		}
	}

	private String createNotificationForAdmin(String text, User user) {
		return new StringBuilder()
				.append("Пользователь: ")
				.append(user.getUserName())
				.append(" c id: ")
				.append(user.getId())
				.append(" написал вам:\n")
				.append(text)
				.toString();
	}
}