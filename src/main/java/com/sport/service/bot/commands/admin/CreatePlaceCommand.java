package com.sport.service.bot.commands.admin;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.interfaces.PhotoProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.Subdistrict;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.redis_store.commands_store.sessions.PlaceSession;
import com.sport.service.services.PlaceService;
import com.sport.service.services.SubscriberService;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
	private final SubscriberService subscriberService;

	private final TelegramMessageSender sender;

	@Value("${telegram.bot.token}")
	private String botToken;

	@Override
	public String getCommandIdentifier() {
        return CommandsConstants.CREATE_PLACE;
	}

	@Override
	public String getDescription() {
        return CommandsConstants.CREATE_PLACE_DESCRIPTION;
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
            commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
			answer.setText("creating");
			answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboard(answer));
		}
		try {
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error sending initial message", e);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
		}
	}

	@Override
	public void processCallback(AbsSender absSender, CallbackQuery callback) {
		Long chatId = callback.getMessage().getChatId();
		Long userId = callback.getFrom().getId();

        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) {
			log.warn("User {} not in create_place session", userId);
			return;
		}

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (dto == null) {
			log.warn("No session found for chatId: {}", chatId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
			commandStateStore.clearCurrentCommand(userId);
			return;
		}

		String data = callback.getData();
		log.info("Processing callback for create_place: step={}, data={}", dto.getStep(), data);

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        if (KeyboardConstants.BACK.equals(data)) { //user wants to back the previous menu to reconsider his choice
			answer.setText("creating");
			try {
				switch (dto.getStep()) {
					case 2 -> { //If user chose in subdistricts menu to choose district again
						dto.setSubdistrict(null);
						dto.setDistrict(null);
						answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboard(answer));
						dto.setStep(1);
					}
					case 3 -> { //If user chose in types of place menu to choose district/subdistrict again
						dto.setPlaceType(null);
						if (dto.getDistrict().hasSubdistricts()) {
							answer.setReplyMarkup(dto.getDistrict().buildSubdistrictsKeyboard(answer));
							dto.setStep(2);
						} else {
							answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboard(answer));
							dto.setStep(1);
						}
					}
					case 4 -> { //If user chose in outdoor/inside places menu to choose type of place again
						dto.setOutdoor(null);
						answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createPlaceTypeKeyboard(answer));
						dto.setStep(3);
					}
				}
				placeSession.save(chatId, dto);
				absSender.execute(answer);
			} catch (TelegramApiException e) {
				log.error("Error sending back step", e);
			}
			return;
		}
		try {
			answer.setText("creating");
			switch (dto.getStep()) {
				case 1 -> handleDistrictStep(dto, data, answer);
				case 2 -> handleSubdistrictStep(dto, data, answer);
				case 3 -> handlePlaceTypeStep(dto, data, answer);
				case 4 -> handleOutdoorStep(dto, data, answer);
				default -> handleUnknownStep(chatId, userId, answer);
			}
			placeSession.save(chatId, dto);
			absSender.execute(answer);
		} catch (TelegramApiException e) {
			log.error("Error processing callback", e);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
		}
	}

	private void handleDistrictStep(PlaceDto dto, String data, SendMessage answer) {
        dto.setDistrict(District.valueOf(data));
		if (dto.getDistrict().hasSubdistricts()) {
			answer.setReplyMarkup(dto.getDistrict().buildSubdistrictsKeyboard(answer));
			dto.setStep(2);
		} else {
			dto.setSubdistrict(null);
			answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createPlaceTypeKeyboard(answer));
			dto.setStep(3);
		}
	}

	private void handleSubdistrictStep(PlaceDto dto, String data, SendMessage answer) {
		dto.setSubdistrict(Subdistrict.valueOf(data));
		answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createPlaceTypeKeyboard(answer));
		dto.setStep(3);
	}

	private void handlePlaceTypeStep(PlaceDto dto, String data, SendMessage answer) {
        dto.setPlaceType(PlaceType.valueOf(data));
		answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createOutdoorKeyboard(answer));
		dto.setStep(4);
	}

	private void handleOutdoorStep(PlaceDto dto, String data, SendMessage answer) {
		dto.setOutdoor(Boolean.parseBoolean(data));
        answer.setText("\uD83D\uDD8A Введите название места:");
		dto.setStep(5);
	}

	private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
		answer.setText(ErrorConstants.UNKNOWN_STEP);
		placeSession.clear(chatId);
		commandStateStore.clearCurrentCommand(userId);
	}

	@Override
	public void processTextInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (dto == null) {
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
			commandStateStore.clearCurrentCommand(userId);
			return;
		}

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

		try {
			handleTextInput(message, dto, answer);
			placeSession.save(chatId, dto);
			absSender.execute(answer);
		} catch (TelegramApiException e) {
			log.error("Error processing text input", e);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
		}
	}

	private void handleTextInput(Message message, PlaceDto dto, SendMessage answer) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();
		String text = message.getText();

		switch (dto.getStep()) {
			case 5 -> {
				if (!placeService.existsByName(text)) {
					dto.setName(text);
                    answer.setText("\uD83D\uDD8A Введите адрес:");
					dto.setStep(6);
				} else {
                    answer.setText("Место с таким названием уже существует❗" + "\n" + "Попробуйте еще раз:");
				}
			}
			case 6 -> {
				dto.setAddress(text);
                answer.setText("\uD83D\uDD8A Введите описание:");
				dto.setStep(7);
			}
			case 7 -> {
				dto.setDescription(text);
				answer.setText("\uD83D\uDD8A Введите сайт (или '-' если его нет):");
				dto.setStep(8);
			}
			case 8 -> {
				dto.setWebSite(text);
				answer.setText("\uD83D\uDCCD Введите координаты места, например их можно взять из Google maps (пример данных: 51.672628201614216, 39.261582161907924 или '-' если их нет):");
				dto.setStep(9);
			}
			case 9 -> {
                dto.setCoordinates(text);
                answer.setText("\uD83D\uDDBC Отправьте фото:");
				dto.setStep(10);
			}
			case 10 -> answer.setText("\uD83D\uDDBC Пожалуйста, отправьте фото:");
			default -> {
				answer.setText(ErrorConstants.UNKNOWN_STEP);
				placeSession.clear(chatId);
				commandStateStore.clearCurrentCommand(userId);
			}
		}
	}

	@Override
	public void processPhotoInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (dto == null || dto.getStep() != 10) {
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
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
				placeService.create(dto);
                answer.setText("Место создано ✅");
				placeSession.clear(chatId);
				commandStateStore.clearCurrentCommand(userId);
			}
			absSender.execute(answer);
        } catch (TelegramApiException e) {
			log.error("Error processing photo", e);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.UNEXPECTED_PHOTO);
		}
	}

    private byte[] downloadPhoto(AbsSender absSender, String fileId) {
		GetFile getFileMethod = new GetFile();
		getFileMethod.setFileId(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = null;
        try {
            file = absSender.execute(getFileMethod);
        } catch (TelegramApiException e) {
            log.error("Error download photo", e);
        }
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
        } catch (IOException e) {
			log.error("Failed to download photo from URL: {}", fileUrl, e);
			throw new RuntimeException("Failed to download photo: " + e.getMessage(), e);
		}
	}
}