package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.CreatePlaceStep;
import com.sport.service.entities.enums.place.PlaceState;
import com.sport.service.store.commands.CommandStateStore;
import com.sport.service.store.commands.sessions.PlaceSession;
import com.sport.service.services.NotificationCreatorService;
import com.sport.service.services.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetPlaceCommand implements IBotCommand, CallbackProcessable, TextProcessable {
	private final CommandStateStore commandStateStore;
	private final PlaceSession placeSession;

	private final PlaceService placeService;
	private final NotificationCreatorService notificationCreatorService;

	private final TelegramMessageSender sender;

	@Override
	public String getCommandIdentifier() {
		return CommandsConstants.GET_PLACE;
	}

	@Override
	public String getDescription() {
		return CommandsConstants.GET_PLACE_DESCRIPTION;
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		User user = message.getFrom();
		Long chatId = message.getChatId();
		Long userId = user.getId();
		log.info("Call command get_place by userId={}, username={}", userId, user.getUserName());

		PlaceState state = new PlaceState();
		state.setStep(CreatePlaceStep.DISTRICT);
		placeSession.save(chatId, state);
		commandStateStore.setCurrentCommand(userId, getCommandIdentifier());

		showStepMenu(chatId, state);
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
		log.info("Processing callback for get_place: step={}, data={}", state.getStep(), data);

		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());
		answer.setText(CommandsConstants.GETTING_TYPE);

		try {
			if (KeyboardConstants.BACK.equals(data)) {
				handleBack(state, answer, absSender, chatId, userId);
				return;
			}

			CreatePlaceStep currentStep = state.getStep();
			if (currentStep.isCallbackStep()) {
				CreatePlaceStep nextStep = currentStep.onCallback(data, state, placeService);
				state.setStep(nextStep);
				showStepMenu(chatId, state);
			}
		} catch (Exception e) {
			log.error("Error processing callback in get_place", e);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
		}
	}

	private void showStepMenu(Long chatId, PlaceState state) {
		CreatePlaceStep step = state.getStep();
		SendMessage answer = new SendMessage();
		answer.setChatId(chatId.toString());
		answer.setText(CommandsConstants.GETTING_TYPE);

		try {
			InlineKeyboardMarkup keyboard = step.buildKeyboard(answer, state);
			answer.setReplyMarkup(keyboard);

			if (step.isFinished(state)) {
				handlePlaces(chatId, state);
				cleanupSession(chatId);
			} else {
				try {
					absSender.execute(answer);
				} catch (TelegramApiException e) {
					log.error("Error showing step menu", e);
				}
			}

			placeSession.save(chatId, state);
		} catch (Exception e) {
			log.error("Error showing step menu", e);
			handleUnknownStep(chatId, state);
		}
	}

	private void handleBack(PlaceState state, SendMessage answer, AbsSender absSender, Long chatId, Long userId) {
		CreatePlaceStep currentStep = state.getStep();

		switch (currentStep) {
			case PLACE_TYPE:
				state.setPlaceType(null);
				if (state.getDistrict().hasSubdistricts()) {
					state.setStep(CreatePlaceStep.SUBDISTRICT);
				} else {
					state.setStep(CreatePlaceStep.DISTRICT);
				}
				break;
			case SUBDISTRICT:
				state.setSubDistrict(null);
				state.setStep(CreatePlaceStep.DISTRICT);
				break;
			case OUTDOOR:
				state.setOutdoor(null);
				state.setStep(CreatePlaceStep.PLACE_TYPE);
				break;
			default:
				return;
		}

		state.setStep(state.getStep().getPrevious());

		try {
			InlineKeyboardMarkup keyboard = state.getStep().buildKeyboard(answer, state);
			answer.setReplyMarkup(keyboard);
			absSender.execute(answer);
			placeSession.save(chatId, state);
		} catch (Exception e) {
			log.error("Error in back navigation", e);
		}
	}

	private void handleUnknownStep(Long chatId, PlaceState state) {
		sender.sendMessageWithoutPhoto(chatId, ErrorConstants.UNKNOWN_STEP);
		placeSession.clear(chatId);
	}

	private void handlePlaces(Long chatId, PlaceState state) {
		District district = state.getDistrict();
		SubDistrict subDistrict = state.getSubDistrict();
		PlaceType placeType = state.getPlaceType();
		Boolean outdoor = state.getOutdoor();

		List<Place> places = new ArrayList<>();

		if (district.equals(District.ALL_DISTRICTS) && outdoor == null) {
			places = placeService.findAllByPlaceType(placeType);
		}

		if (district.equals(District.ALL_DISTRICTS) && outdoor != null) {
			places = placeService.findAllByPlaceTypeAndOutdoor(placeType, outdoor);
		}

		if (!district.equals(District.ALL_DISTRICTS) && subDistrict != SubDistrict.ALL_SUBDISTRICTS && outdoor != null) {
			places = placeService.findByDistrictAndSubDistrictAndPlaceTypeAndOutdoor(
					district, subDistrict, placeType, outdoor);
		}

		if (!district.equals(District.ALL_DISTRICTS) && subDistrict != SubDistrict.ALL_SUBDISTRICTS && outdoor == null) {
			places = placeService.findByDistrictAndSubDistrictAndPlaceType(
					district, subDistrict, placeType);
		}

		if (!district.equals(District.ALL_DISTRICTS) && (subDistrict == null || subDistrict.equals(SubDistrict.ALL_SUBDISTRICTS)) && outdoor != null) {
			places = placeService.findByDistrictAndPlaceTypeAndOutdoor(
					district, placeType, outdoor);
		}

		if (!district.equals(District.ALL_DISTRICTS) && (subDistrict == null || subDistrict.equals(SubDistrict.ALL_SUBDISTRICTS)) && outdoor == null) {
			places = placeService.findByDistrictAndPlaceType(
					district, placeType);
		}

		if (places.isEmpty()) {
			sender.sendMessageWithoutPhoto(chatId, CommandsConstants.NO_PLACES);
			return;
		}

		for (Place place : places) {
			sendPlaceInfo(chatId, place);
		}
	}

	private void sendPlaceInfo(Long chatId, Place place) {
		byte[] photo = place.getPhoto();
		if (photo != null && photo.length > 0) {
			String caption = createCaption(place);
			sender.sendMessageWithPhoto(chatId, photo, caption);
		} else {
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
		}
	}

	private String createCaption(Place place) {
		String mapLink = null;
		if (!place.getCoordinates().equals("-")) {
			String[] coordinates = place.getCoordinates().split(",");
			float latitude = Float.parseFloat(coordinates[0].trim());
			float longitude = Float.parseFloat(coordinates[1].trim());
			mapLink = String.format("https://maps.google.com/?q=%f,%f", latitude, longitude);
		}

		return notificationCreatorService.createPlaceMessage(place, mapLink);
	}

	private void cleanupSession(Long chatId) {
		placeSession.clear(chatId);
		commandStateStore.clearCurrentCommand(chatId);
	}

	private boolean ifSessionValid(Long chatId, PlaceState state) {
		if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(chatId))) {
			log.warn("User {} is not in get_place session", chatId);
			return false;
		}

		if (state == null) {
			log.warn("No session found for chatId: {}", chatId);
			sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
			commandStateStore.clearCurrentCommand(chatId);
			return false;
		}
		return true;
	}

	@Override
	public void processTextInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();

		PlaceState state = placeSession.getIfExists(chatId);
		if (!ifSessionValid(chatId, state) || state == null) {
			return;
		}

		CreatePlaceStep currentStep = state.getStep();
		if (currentStep == CreatePlaceStep.DISTRICT || currentStep == CreatePlaceStep.SUBDISTRICT
				|| currentStep == CreatePlaceStep.PLACE_TYPE) {
			sendMessageWithoutPhoto(chatId, "Пожалуйста, используйте кнопку меню для выбора: " + currentStep.name());
		}
	}
}
