package com.sport.service.bot.commands.admin;
import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.services.SubscriberService;
import com.sport.service.sessions.PlaceSession;
import com.sport.service.sessions.CommandStateStore;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.*;
import com.sport.service.services.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.sport.service.entities.place.District;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class CreatePlaceCommand implements IBotCommand {

	private final PlaceSession placeSession;

	private final PlaceService placeService;

	private final CommandStateStore commandStateStore;

	private final PlaceMapper placeMapper;

	private final SubscriberService subscriberService;

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
		log.info("Call command create_place by user: {}", user.getUserName());
		Long chatId = message.getChatId();
		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());

		if (subscriberService.checkIfAdmin(user.getId())) {
			PlaceDto dto = placeSession.createSession(chatId);
			dto.setStep(1);
			placeSession.save(chatId, dto);
			commandStateStore.setCurrentCommand(user.getId(), "create_place");
			answer.setText("Выберите район:");
			answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboardForCreatingPlace());
		} else {
			answer.setText("Вы не являетесь администратором.");
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

		if (!"create_place".equals(commandStateStore.getCurrentCommand(userId))) {
			log.warn("User {} not in create_place session", userId);
			return;
		}

		PlaceDto dto = placeSession.getIfExists(chatId);

		if (dto == null) {
			log.warn("No session found for chatId: {}", chatId);
			sendErrorMessage(absSender, chatId, "Сессия истекла. Начните заново с /create_place");
			commandStateStore.clearCurrentCommand(userId);
			return;
		}

		String data = callback.getData();
		log.info("Processing callback for create_place: step={}, data={}", dto.getStep(), data);

		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());

		// Handle BACK navigation between callback-driven steps (1..3)
		if ("BACK".equals(data)) {
			if (dto.getStep() == 2) {
				// Going back from type selection to district
				dto.setType(null);
				answer.setText("Выберите район:");
				answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboardForCreatingPlace());
				dto.setStep(1);
			} else if (dto.getStep() == 3) {
				// Going back from outdoor selection to type
				dto.setOutdoor(null);
				answer.setText("Выберите тип места:");
				answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createTypeKeyboard());
				dto.setStep(2);
			} else {
				// At first step, nothing to go back to; re-show districts
				answer.setText("Выберите район:");
				answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboardForCreatingPlace());
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
			sendErrorMessage(absSender, chatId, "Произошла ошибка. Попробуйте еще раз.");
		}
	}

	private void handleDistrictStep(PlaceDto dto, String data, SendMessage answer) {
		try {
			dto.setDistrict(District.valueOf(data));
			answer.setText("Выберите тип места:");
			answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createTypeKeyboard());
			dto.setStep(2);
		} catch (IllegalArgumentException e) {
			answer.setText("Неверный район. Попробуйте еще раз:");
			answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboardForCreatingPlace());
		}
	}

	private void handleTypeStep(PlaceDto dto, String data, SendMessage answer) {
		try {
			dto.setType(Type.valueOf(data));
			answer.setText("Это улица или помещение?");
			answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createOutdoorKeyboardForCreatingPlace());
			dto.setStep(3);
		} catch (IllegalArgumentException e) {
			answer.setText("Неверный тип. Попробуйте еще раз:");
			answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createTypeKeyboard());
		}
	}

	private void handleOutdoorStep(PlaceDto dto, String data, SendMessage answer) {
		dto.setOutdoor(Boolean.parseBoolean(data));
		answer.setText("Введите название места:");
		dto.setStep(4);
	}

	private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
		answer.setText("Неизвестный шаг. Начните заново с /create_place");
		placeSession.clear(chatId);
		commandStateStore.clearCurrentCommand(userId);
	}

	public void processTextInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

		if (!"create_place".equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (dto == null) {
			sendErrorMessage(absSender, chatId, "Сессия истекла. Начните заново с /create_place");
			commandStateStore.clearCurrentCommand(userId);
			return;
		}

		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());
		try {
			handleTextInput(message, dto, answer);
			placeSession.save(chatId, dto);
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error processing text input", e);
			sendErrorMessage(absSender, chatId, "Ошибка при обработке ввода. Попробуйте еще раз.");
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
					answer.setText("Введите адрес:");
					dto.setStep(5);
				} else {
					answer.setText("Место с таким названием уже существует. Попробуйте еще раз:");
				}
			}
			case 5 -> {
				dto.setAddress(text);
				answer.setText("Введите описание:");
				dto.setStep(6);
			}
			case 6 -> {
				dto.setDescription(text);
				answer.setText("Введите сайт (или '-' если нет):");
				dto.setStep(7);
			}
			case 7 -> {
				dto.setWebSite(text);
				answer.setText("Отправьте фото:");
				dto.setStep(8);
			}
			case 8 -> {
				answer.setText("Пожалуйста, отправьте фото:");
			}
			default -> {
				answer.setText("Неизвестный шаг. Начните заново с /create_place");
				placeSession.clear(chatId);
				commandStateStore.clearCurrentCommand(userId);
			}
		}
	}

	public void processPhotoInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

		if (!"create_place".equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (dto == null || dto.getStep() != 8) {
			sendErrorMessage(absSender, chatId, "Неожиданное фото. Начните заново с /create_place");
			commandStateStore.clearCurrentCommand(userId);
			return;
		}

		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());

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
				answer.setText("✅ Место создано!");
				placeSession.clear(chatId);
				commandStateStore.clearCurrentCommand(userId);
			} else {
				answer.setText("Пожалуйста, отправьте фото:");
			}
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error processing photo", e);
			sendErrorMessage(absSender, chatId, "❌ Ошибка при создании места: " + e.getMessage());
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
			byte[] buffer = new byte[8192]; // Increased buffer size for better performance
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