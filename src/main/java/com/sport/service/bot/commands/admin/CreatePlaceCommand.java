package com.sport.service.bot.commands.admin;

import com.sport.service.annotations.AdminOnly;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.UtilMethods;
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
import com.sport.service.entities.enums.place.SubDistrict;
import com.sport.service.store.commands.CommandStateStore;
import com.sport.service.store.commands.sessions.PlaceSession;
import com.sport.service.services.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class CreatePlaceCommand implements IBotCommand, PhotoProcessable, TextProcessable, CallbackProcessable {
	private final PlaceSession placeSession;
	private final PlaceService placeService;

	private final CommandStateStore commandStateStore;

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

	@AdminOnly
	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		User user = message.getFrom();
		Long chatId = message.getChatId();
		Long userId = user.getId();
		log.info("Call command create_place by userId={}, username={}", userId, user.getUserName());

		try {
			PlaceDto dto = placeSession.createSession(chatId);
			dto.setStep(1);
			placeSession.save(chatId, dto);
			commandStateStore.setCurrentCommand(userId, getCommandIdentifier());

			SendMessage answer = new SendMessage();
			answer.setChatId(chatId.toString());
			answer.setText(CommandsConstants.CREATING_TYPE);
			answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboard(answer));

			sender.sendMessageWithoutPhoto(answer);
		} catch (Exception e) {
			log.error("Error to start processing message", e);
			placeSession.clear(userId);
			commandStateStore.clearCurrentCommand(userId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
		}
	}

	@Override
	public void processCallback(AbsSender absSender, CallbackQuery callback) {
		Long chatId = callback.getMessage().getChatId();
		Long userId = callback.getFrom().getId();

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (!ifSessionValid(chatId, dto)) {
			return;
		}

		String data = callback.getData();
		log.info("Processing callback for create_place: step={}, data={}", dto.getStep(), data);

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

		try {
			if (KeyboardConstants.BACK.equals(data)) { //User wants to back the previous menu to reconsider his choice
				answer.setText(CommandsConstants.CREATING_TYPE);
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
				sender.sendMessageWithoutPhoto(answer);
				return;
			}

			answer.setText(CommandsConstants.CREATING_TYPE);
			switch (dto.getStep()) {
				case 1 -> handleDistrictStep(dto, data, answer);
				case 2 -> handleSubdistrictStep(dto, data, answer);
				case 3 -> handlePlaceTypeStep(dto, data, answer);
				case 4 -> handleOutdoorStep(dto, data, answer);
				default -> handleUnknownStep(chatId, userId, answer);
			}
			placeSession.save(chatId, dto);
			sender.sendMessageWithoutPhoto(answer);
		} catch (Exception e) {
			log.error("Error processing callback", e);
			placeSession.clear(userId);
			commandStateStore.clearCurrentCommand(userId);
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
        dto.setSubdistrict(SubDistrict.valueOf(data));
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
        answer.setText(CommandsConstants.ENTER_PLACE_NAME);
		dto.setStep(5);
	}

	@Override
	public void processTextInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (!ifSessionValid(chatId, dto)) {
			return;
		}

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

		try {
			handleTextInput(message, dto, answer);
			placeSession.save(chatId, dto);
			sender.sendMessageWithoutPhoto(answer);
		} catch (Exception e) {
			log.error("Error processing text input", e);
			placeSession.clear(userId);
			commandStateStore.clearCurrentCommand(userId);
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
                    answer.setText(CommandsConstants.ENTER_PLACE_ADDRESS);
					dto.setStep(6);
				} else {
                    answer.setText(CommandsConstants.PLACE_NAME_IS_EXISTED);
				}
			}
			case 6 -> {
				dto.setAddress(text);
                answer.setText(CommandsConstants.ENTER_PLACE_DESCRIPTION);
				dto.setStep(7);
			}
			case 7 -> {
				dto.setDescription(text);
				answer.setText(CommandsConstants.ENTER_PLACE_LINK);
				dto.setStep(8);
			}
			case 8 -> {
				dto.setWebSite(text.equals("-") ? null : text);
				answer.setText(CommandsConstants.ENTER_PLACE_COORDINATES);
				dto.setStep(9);
			}
			case 9 -> {
                dto.setCoordinates(text);
                answer.setText(CommandsConstants.SEND_PLACE_PHOTO);
				dto.setStep(10);
			}
			case 10 -> answer.setText(CommandsConstants.SEND_PLACE_PHOTO_2);
			default -> handleUnknownStep(chatId, userId, answer);
		}
	}

	private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
		answer.setText(ErrorConstants.UNKNOWN_STEP);
		placeSession.clear(chatId);
		commandStateStore.clearCurrentCommand(userId);
	}

	@Override
	public void processPhotoInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

		PlaceDto dto = placeSession.getIfExists(chatId);
		if (!ifSessionValid(chatId, dto)) {
			return;
		}

		try {
			if (message.hasPhoto()) {
				List<PhotoSize> photos = message.getPhoto();
				PhotoSize bestPhoto = photos.get(photos.size() - 1);
				String fileId = bestPhoto.getFileId();
				log.info("Processing photo: {} sizes available, selected size: {}x{}, file size: {} bytes",
						photos.size(), bestPhoto.getWidth(), bestPhoto.getHeight(), bestPhoto.getFileSize());
				byte[] photoBytes = UtilMethods.downloadPhoto(absSender, fileId, botToken);
				log.info("Downloaded photo: {} bytes", photoBytes.length);

				dto.setPhoto(photoBytes);
				placeService.create(dto);
				placeSession.clear(chatId);
				commandStateStore.clearCurrentCommand(userId);
				sender.sendMessageWithoutPhoto(chatId, CommandsConstants.PLACE_CREATED);
			}
		} catch (Exception e) {
			log.error("Error processing photo", e);
			placeSession.clear(chatId);
			commandStateStore.clearCurrentCommand(userId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.UNEXPECTED_PHOTO);
		}
	}

	private boolean ifSessionValid(Long chatId, PlaceDto dto) {
		if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(chatId))) {
			log.warn("User {} is not in create_place session", chatId);
			return false;
		}

		if (dto == null) {
			log.warn("No session found for chatId: {}", chatId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
			commandStateStore.clearCurrentCommand(chatId);
			return false;
		}
		return true;
	}
}