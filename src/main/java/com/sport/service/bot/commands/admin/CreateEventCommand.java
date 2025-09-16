package com.sport.service.bot.commands.admin;

import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.dto.EventDto;
import com.sport.service.entities.place.District;
import com.sport.service.mappers.event.EventMapper;
import com.sport.service.services.EventService;
import com.sport.service.services.SubscriberService;
import com.sport.service.sessions.CommandStateStore;
import com.sport.service.sessions.EventSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
@Slf4j
public class CreateEventCommand implements IBotCommand {

	private final EventSession eventSession;

	private final EventService eventService;

	private final CommandStateStore commandStateStore;

	private final EventMapper eventMapper;

	private final SubscriberService subscriberService;

    private final Pattern DATE_PATTERN = Pattern.compile(
            "^([1-9]\\d{3})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");

    private final Pattern TIME_PATTERN = Pattern.compile(
            "^([01]\\d|2[0-3]):([0-5]\\d)$");

    private final String sessionExpired = "Сессия истекла. Начните заново с /create_event \uD83D\uDD04";

    private final String unknownStep = "Неизвестный шаг. Начните заново с /create_event \uD83D\uDD04";

	@Override
	public String getCommandIdentifier() {
		return "create_event";
	}

	@Override
	public String getDescription() {
		return "Let admin create a new event";
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		User user = message.getFrom();
        Long userId = user.getId();
		Long chatId = message.getChatId();
        log.info("Call command create_event by userId={}, username={}", userId, user.getUserName());
        SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());

        if (subscriberService.checkIfAdmin(userId)) {
			EventDto dto = eventSession.createSession(chatId);
			dto.setStep(1);
			eventSession.save(chatId, dto);
            commandStateStore.setCurrentCommand(userId, "create_event");
            answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboardForCreating(answer));
		}
		try {
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error sending initial message", e);
		}
	}

	public void processCallback(AbsSender absSender, CallbackQuery callback) {
		Long chatId = callback.getMessage().getChatId();
		Long userId = callback.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

		if (!"create_event".equals(commandStateStore.getCurrentCommand(userId))) {
			log.warn("User {} not in create_event session", userId);
			return;
		}

		EventDto dto = eventSession.getIfExists(chatId);
		if (dto == null) {
			log.warn("No session found for chatId: {}", chatId);
            sendErrorMessage(absSender, chatId, sessionExpired);
			commandStateStore.clearCurrentCommand(userId);
			return;
		}

		String data = callback.getData();
		log.info("Processing callback for create_event: step={}, data={}", dto.getStep(), data);

		try {
			if (dto.getStep() == 1) {
				handleDistrictStep(dto, data, answer);
			} else {
				handleUnknownStep(chatId, userId, answer);
			}
			eventSession.save(chatId, dto);
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error processing callback", e);
            sendErrorMessage(absSender, chatId, "Произошла ошибка. Попробуйте еще раз \uD83D\uDD04");
		}
	}

	private void handleDistrictStep(EventDto dto, String data, SendMessage answer) {
        dto.setDistrict(District.valueOf(data));
        answer.setText("\uD83D\uDD8A Введите наименование события:");
        dto.setStep(2);
	}

	public void processTextInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

		if (!"create_event".equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		EventDto dto = eventSession.getIfExists(chatId);
		if (dto == null) {
            sendErrorMessage(absSender, chatId, sessionExpired);
			commandStateStore.clearCurrentCommand(userId);
			return;
		}

		try {
			handleTextInput(message, dto, answer);
			eventSession.save(chatId, dto);
			absSender.execute(answer);

			if (dto.getStep() > 8) {
				eventService.create(eventMapper.eventDtoToEvent(dto));
				SendMessage successMsg = new SendMessage();
				successMsg.setChatId(chatId.toString());
                successMsg.setText("Событие создано ✅");
				absSender.execute(successMsg);
                log.info("Event created successfully by userId={}, eventName={}", userId, dto.getName());
				eventSession.clear(chatId);
				commandStateStore.clearCurrentCommand(userId);
			}
		} catch (Exception e) {
			log.error("Error processing text input", e);
            sendErrorMessage(absSender, chatId, "Ошибка при обработке ввода. Попробуйте еще раз \uD83D\uDD04");
		}
	}

	private void handleTextInput(Message message, EventDto dto, SendMessage answer) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();
		String text = message.getText();

		switch (dto.getStep()) {
			case 2 -> {
				dto.setName(text);
                answer.setText("\uD83D\uDD8A Введите адрес:");
				dto.setStep(3);
			}
			case 3 -> {
				dto.setAddress(text);
                answer.setText("\uD83D\uDD8A Введите описание:");
				dto.setStep(4);
			}
			case 4 -> {
				dto.setDescription(text);
                answer.setText("\uD83D\uDD8A Введите ссылку на событие (или '-' если нет):");
				dto.setStep(5);
			}
			case 5 -> {
				dto.setLink(text);
                answer.setText("\uD83D\uDD8A Введите имя места где будет организовано событие:");
				dto.setStep(6);
			}
			case 6 -> {
				dto.setPlaceName(text);
                answer.setText("\uD83D\uDD8A Введите дату в формате YYYY-MM-DD:");
				dto.setStep(7);
			}
			case 7 -> {
				if (isValidDate(text)) {
					dto.setDate(text);
                    answer.setText("\uD83D\uDD8A Введите время в формате HH:mm:");
					dto.setStep(8);
				} else {
					answer.setText("Неверный формат даты. Введите дату в формате YYYY-MM-DD:");
				}
			}
			case 8 -> {
				if (isValidTime(text)) {
					dto.setTime(text);
                    answer.setText("Все данные получены. Создаю событие ⏳");
					dto.setStep(9);
				} else {
					answer.setText("Неверный формат времени. Введите время в формате HH:mm:");
				}
			}
            default -> handleUnknownStep(chatId, userId, answer);
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

	private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
        answer.setText(unknownStep);
		eventSession.clear(chatId);
		commandStateStore.clearCurrentCommand(userId);
	}

	private boolean isValidDate(String date) {
		return DATE_PATTERN.matcher(date).matches();
	}

	private boolean isValidTime(String time) {
		return TIME_PATTERN.matcher(time).matches();
	}
}