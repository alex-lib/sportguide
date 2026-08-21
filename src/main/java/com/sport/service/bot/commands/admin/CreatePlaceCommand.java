package com.sport.service.bot.commands.admin;

import com.sport.service.annotations.AdminOnly;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.UtilMethods;
import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.interfaces.PhotoProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.CreatePlaceStep;
import com.sport.service.entities.enums.place.PlaceState;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
			PlaceState state = new PlaceState();
			state.setStep(CreatePlaceStep.DISTRICT);
			placeSession.save(chatId, state);
			commandStateStore.setCurrentCommand(userId, getCommandIdentifier());

			SendMessage answer = new SendMessage();
			answer.setChatId(chatId.toString());
			answer.setText(CommandsConstants.CREATING_TYPE);

			InlineKeyboardMarkup keyboard = CreatePlaceStep.DISTRICT.buildKeyboard(answer, state);
			answer.setReplyMarkup(keyboard);

			sender.sendMessageWithoutPhoto(answer);
		} catch (Exception e) {
			log.error("Error to start processing message", e);
			placeSession.clear(chatId);
			commandStateStore.clearCurrentCommand(userId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
		}
	}

	@Override
	public void processCallback(AbsSender absSender, CallbackQuery callback) {
		Long chatId = callback.getMessage().getChatId();
		Long userId = callback.getFrom().getId();

		PlaceState state = placeSession.getIfExists(chatId);
		if (!ifSessionValid(chatId, state)) {
			return;
		}

		String data = callback.getData();
		log.info("Processing callback for create_place: step={}, data={}", state.getStep(), data);

		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());
		answer.setText(CommandsConstants.CREATING_TYPE);

		try {
			if (KeyboardConstants.BACK.equals(data)) {
				handleBack(state, answer);
				placeSession.save(chatId, state);
				sender.sendMessageWithoutPhoto(answer);
				return;
			}

			CreatePlaceStep currentStep = state.getStep();
			if (currentStep.isCallbackStep()) {
				CreatePlaceStep nextStep = currentStep.onCallback(data, state, placeService);
				state.setStep(nextStep);
				InlineKeyboardMarkup keyboard = nextStep.buildKeyboard(answer, state);
				answer.setReplyMarkup(keyboard);
			} else {
				handleUnknownStep(chatId, userId, answer);
			}

			placeSession.save(chatId, state);
			sender.sendMessageWithoutPhoto(answer);
		} catch (Exception e) {
			log.error("Error processing callback", e);
			placeSession.clear(chatId);
			commandStateStore.clearCurrentCommand(userId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
		}
	}

	private void handleBack(PlaceState state, SendMessage answer) {
		CreatePlaceStep currentStep = state.getStep();
		CreatePlaceStep previousStep = currentStep.PREV;

		if (previousStep == null) {
			return;
		}

		state.setStep(previousStep);

		// Reset state based on previous step
		switch (previousStep) {
			case DISTRICT:
				state.setSubDistrict(null);
				state.setPlaceType(null);
				state.setOutdoor(null);
				state.setDistrict(null);
				answer.setReplyMarkup(previousStep.buildKeyboard(answer, state));
				break;
			case SUBDISTRICT:
				state.setPlaceType(null);
				state.setOutdoor(null);
				answer.setReplyMarkup(previousStep.buildKeyboard(answer, state));
				break;
			case PLACE_TYPE:
				state.setOutdoor(null);
				answer.setReplyMarkup(previousStep.buildKeyboard(answer, state));
				break;
			case OUTDOOR:
				answer.setReplyMarkup(previousStep.buildKeyboard(answer, state));
				break;
			default:
				break;
		}
	}

	@Override
	public void processTextInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

		PlaceState state = placeSession.getIfExists(chatId);
		if (!ifSessionValid(chatId, state)) {
			return;
		}

		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());
		answer.setText(CommandsConstants.CREATING_TYPE);

		String text = message.getText();

		try {
			CreatePlaceStep currentStep = state.getStep();

			if (currentStep.isTextStep()) {
				String nextStepName = currentStep.handleText(answer, text, state, placeService);

				if (nextStepName != null) {
					CreatePlaceStep nextStep = CreatePlaceStep.valueOf(nextStepName);
					state.setStep(nextStep);

					InlineKeyboardMarkup keyboard = nextStep.buildKeyboard(answer, state);
					answer.setReplyMarkup(keyboard);
				}

				placeSession.save(chatId, state);

				if (!answer.getText().isEmpty()) {
					sender.sendMessageWithoutPhoto(answer);
				}
			} else {
				handleUnknownStep(chatId, userId, answer);
				sender.sendMessageWithoutPhoto(answer);
			}
		} catch (Exception e) {
			log.error("Error processing text input", e);
			placeSession.clear(chatId);
			commandStateStore.clearCurrentCommand(userId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
		}
	}

	@Override
	public void processPhotoInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

		PlaceState state = placeSession.getIfExists(chatId);
		if (!ifSessionValid(chatId, state)) {
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

				state.setPhoto(photoBytes);
				placeService.create(state);
				placeSession.clear(chatId);
				commandStateStore.clearCurrentCommand(userId);
				sender.sendMessageWithoutPhoto(chatId, CommandsConstants.PLACE_CREATED);
			} else {
				sender.sendMessageWithoutPhoto(chatId, CommandsConstants.SEND_PLACE_PHOTO_2);
			}
		} catch (Exception e) {
			log.error("Error processing photo", e);
			placeSession.clear(chatId);
			commandStateStore.clearCurrentCommand(userId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.UNEXPECTED_PHOTO);
		}
	}

	private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
		answer.setText(ErrorConstants.UNKNOWN_STEP);
		placeSession.clear(chatId);
		commandStateStore.clearCurrentCommand(userId);
	}

	private boolean ifSessionValid(Long chatId, PlaceState state) {
		if (state == null) {
			log.warn("No session found for chatId: {}", chatId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
			return false;
		}
		return true;
	}
}
