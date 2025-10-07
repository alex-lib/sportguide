package com.sport.service.bot.commands.admin;

import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.interfaces.PhotoProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Type;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.services.PlaceService;
import com.sport.service.services.SubscriberService;
import com.sport.service.sessions.CommandStateStore;
import com.sport.service.sessions.PlaceSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class CreatePlaceCommand implements IBotCommand, PhotoProcessable, TextProcessable, CallbackProcessable {

	private final PlaceSession placeSession;

	private final PlaceService placeService;

	private final CommandStateStore commandStateStore;

	private final PlaceMapper placeMapper;

	private final SubscriberService subscriberService;

	private final String sessionExpired = "Сессия истекла. Начните заново \uD83D\uDD04";

	private final String unknownStep = "Неизвестный шаг. Начните заново \uD83D\uDD04";

	private final String unexpectedPhoto = "Неожиданное фото. Начните заново \uD83D\uDD04";

	@Value("${telegram.bot.token}")
	private String botToken;

	@Override
	public String getCommandIdentifier() {
		return "create_place";
	}

	@Override
	public String getDescription() {
		return "Let admin create a new place";
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		User user = message.getFrom();
		Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command create_place by userId={}, username={}", userId, user.getUserName());
		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());

        if (subscriberService.checkIfAdmin(userId)) {
			PlaceDto dto = placeSession.createSession(chatId);
			dto.setStep(1);
			placeSession.save(chatId, dto);
            commandStateStore.setCurrentCommand(userId, "create_place");
            answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboardForCreating(answer));
		}
		try {
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error sending initial message", e);
		}
	}

	@Override
	public void processCallback(AbsSender absSender, CallbackQuery callback) {
		Long chatId = callback.getMessage().getChatId();
		Long userId = callback.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

		if (!"create_place".equals(commandStateStore.getCurrentCommand(userId))) {
			log.warn("User {} not in create_place session", userId);
			return;
		}

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (dto == null) {
			log.warn("No session found for chatId: {}", chatId);
            sendErrorMessage(absSender, chatId, sessionExpired);
			commandStateStore.clearCurrentCommand(userId);
			return;
		}

		String data = callback.getData();
		log.info("Processing callback for create_place: step={}, data={}", dto.getStep(), data);

		if ("BACK".equals(data)) {
			if (dto.getStep() == 2) {
				dto.setType(null);
                answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboardForCreating(answer));
				dto.setStep(1);
			} else if (dto.getStep() == 3) {
				dto.setOutdoor(null);
                answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createTypeKeyboard(answer));
				dto.setStep(2);
			} else {
                answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboardForCreating(answer));
				dto.setStep(1);
			}
			try {
				placeSession.save(chatId, dto);
				absSender.execute(answer);
			} catch (Exception e) {
				log.error("Error sending back step", e);
			}
			return;
		}

		try {
			switch (dto.getStep()) {
				case 1 -> handleDistrictStep(dto, data, answer);
				case 2 -> handleTypeStep(dto, data, answer);
				case 3 -> handleOutdoorStep(dto, data, answer);
				default -> handleUnknownStep(chatId, userId, answer);
			}
			placeSession.save(chatId, dto);
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error processing callback", e);
            sendErrorMessage(absSender, chatId, "Произошла ошибка. Попробуйте еще раз \uD83D\uDD04");
		}
	}

	private void handleDistrictStep(PlaceDto dto, String data, SendMessage answer) {
        dto.setDistrict(District.valueOf(data));
        answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createTypeKeyboard(answer));
        dto.setStep(2);
	}

	private void handleTypeStep(PlaceDto dto, String data, SendMessage answer) {
        dto.setType(Type.valueOf(data));
        answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createOutdoorKeyboardForCreatingPlace(answer));
        dto.setStep(3);
	}

	private void handleOutdoorStep(PlaceDto dto, String data, SendMessage answer) {
		dto.setOutdoor(Boolean.parseBoolean(data));
        answer.setText("\uD83D\uDD8A Введите название места:");
		dto.setStep(4);
	}

	private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
        answer.setText(unknownStep);
		placeSession.clear(chatId);
		commandStateStore.clearCurrentCommand(userId);
	}

	@Override
	public void processTextInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

		if (!"create_place".equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (dto == null) {
            sendErrorMessage(absSender, chatId, sessionExpired);
			commandStateStore.clearCurrentCommand(userId);
			return;
		}

		try {
			handleTextInput(message, dto, answer);
			placeSession.save(chatId, dto);
			absSender.execute(answer);

		} catch (Exception e) {
			log.error("Error processing text input", e);
            sendErrorMessage(absSender, chatId, "Ошибка при обработке ввода. Попробуйте еще раз \uD83D\uDD04");
		}
	}

	private void handleTextInput(Message message, PlaceDto dto, SendMessage answer) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();
		String text = message.getText();

		switch (dto.getStep()) {
			case 4 -> {
				if (!placeService.existsByName(text)) {
					dto.setName(text);
                    answer.setText("\uD83D\uDD8A Введите адрес:");
					dto.setStep(5);
				} else {
                    answer.setText("Место с таким названием уже существует❗" + "\n" + "Попробуйте еще раз:");
				}
			}
			case 5 -> {
				dto.setAddress(text);
                answer.setText("\uD83D\uDD8A Введите описание:");
				dto.setStep(6);
			}
			case 6 -> {
				dto.setDescription(text);
                answer.setText("\uD83D\uDD8A Введите сайт (или '-' если его нет):");
				dto.setStep(7);
			}
			case 7 -> {
				dto.setWebSite(text);
                answer.setText("\uD83D\uDCCD Введите координаты места, например их можно взять из Google maps (пример данных: 51.672628201614216, 39.261582161907924 или '-' если их нет):");
				dto.setStep(8);
			}
			case 8 -> {
                dto.setCoordinates(text);
                answer.setText("\uD83D\uDDBC Отправьте фото:");
                dto.setStep(8);
			}
            case 9 -> answer.setText("\uD83D\uDDBC Пожалуйста, отправьте фото:");
			default -> {
                answer.setText(unknownStep);
				placeSession.clear(chatId);
				commandStateStore.clearCurrentCommand(userId);
			}
		}
	}

	@Override
	public void processPhotoInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

		if (!"create_place".equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (dto == null || dto.getStep() != 8) {
            sendErrorMessage(absSender, chatId, unexpectedPhoto);
			commandStateStore.clearCurrentCommand(userId);
			return;
		}

		try {
			if (message.hasPhoto()) {
				List<PhotoSize> photos = message.getPhoto();
				PhotoSize bestPhoto = photos.get(photos.size() - 1);
				String fileId = bestPhoto.getFileId();
                log.info("Processing photo: {} sizes available, selected size: {}x{}, file size: {} bytes",
                        photos.size(), bestPhoto.getWidth(), bestPhoto.getHeight(), bestPhoto.getFileSize());
				byte[] photoBytes = downloadPhoto(absSender, fileId);
				log.info("Downloaded photo: {} bytes", photoBytes.length);
				dto.setPhoto(photoBytes);
				placeService.create(placeMapper.placeDtoToPlace(dto));
                answer.setText("Место создано ✅");
				placeSession.clear(chatId);
				commandStateStore.clearCurrentCommand(userId);
			}
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error processing photo", e);
            sendErrorMessage(absSender, chatId, "Ошибка при создании места ❌");
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

	private byte[] downloadPhoto(AbsSender absSender, String fileId) throws Exception {
		GetFile getFileMethod = new GetFile();
		getFileMethod.setFileId(fileId);
		org.telegram.telegrambots.meta.api.objects.File file = absSender.execute(getFileMethod);
		String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + file.getFilePath();
		log.info("Downloading photo from: {}", fileUrl);
		try (InputStream inputStream = new URL(fileUrl).openStream();
		     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
			int bytesRead;
			long totalBytes = 0;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
				totalBytes += bytesRead;
			}
			byte[] result = outputStream.toByteArray();
			log.info("Successfully downloaded photo: {} bytes", result.length);
			return result;
		} catch (Exception e) {
			log.error("Failed to download photo from URL: {}", fileUrl, e);
			throw new RuntimeException("Failed to download photo: " + e.getMessage(), e);
		}
	}
}