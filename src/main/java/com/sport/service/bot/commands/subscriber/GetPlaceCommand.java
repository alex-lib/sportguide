package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.bot.constants.MenuConstants;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.entities.place.Subdistrict;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.redis_store.commands_store.sessions.PlaceSession;
import com.sport.service.services.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetPlaceCommand implements IBotCommand, CallbackProcessable {
    private final CommandStateStore commandStateStore;
    private final PlaceSession placeSession;

    private final PlaceService placeService;

    @Override
    public String getCommandIdentifier() {
        return "get_place";
    }

    @Override
    public String getDescription() {
        return "Let user to get an appropriate sport place";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command get_place by userId={}, username={}", userId, user.getUserName());

        PlaceDto dto = placeSession.createSession(chatId);
        dto.setStep(1);
        placeSession.save(chatId, dto);
        commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
        showStepMenu(absSender, chatId, dto, userId);
    }

    public void processCallback(AbsSender absSender, CallbackQuery callback) {
        Long chatId = callback.getMessage().getChatId();
        Long userId = callback.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) {
            log.warn("User {} not in get_place session", userId);
            return;
        }

        PlaceDto dto = placeSession.getIfExists(chatId);
        if (dto == null) {
            sendError(absSender, chatId, ErrorConstants.SESSION_EXPIRED);
            commandStateStore.clearCurrentCommand(userId);
            return;
        }

        String data = callback.getData();
        log.info("Processing callback for get_place: step={}, data={}", dto.getStep(), data);

        if (MenuConstants.BACK.equals(data)) { //user wants to back the previous menu to reconsider his choice
            answer.setText("getting");
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
            switch (dto.getStep()) {
                case 1 -> {
                    District district = District.valueOf(data);
                    dto.setDistrict(district);
                    boolean hasSubdistricts = district.hasSubdistricts();
                    if (hasSubdistricts) {
                        dto.setStep(2);
                        placeSession.save(chatId, dto);
                        showStepMenu(absSender, chatId, dto, userId);
                    } else {
                        dto.setSubdistrict(null);
                        dto.setStep(3);
                        placeSession.save(chatId, dto);
                        showStepMenu(absSender, chatId, dto, userId);
                    }
                }
                case 2 -> {
                    dto.setSubdistrict(Subdistrict.valueOf(data));
                    dto.setStep(3);
                    placeSession.save(chatId, dto);
                    showStepMenu(absSender, chatId, dto, userId);
                }
                case 3 -> {
                    dto.setPlaceType(PlaceType.valueOf(data));
                    if (needsOutdoorStep(data)) {
                        dto.setStep(4);
                        placeSession.save(chatId, dto);
                        showStepMenu(absSender, chatId, dto, userId);
                    } else {
                        dto.setStep(5);
                        dto.setOutdoor(null);
                        placeSession.save(chatId, dto);
                        handlePlaces(absSender, chatId, dto);
                        cleanupSession(chatId, userId);
                    }
                }
                case 4 -> {
                    if (data.equals(MenuConstants.NULL)) {
                        dto.setOutdoor(null);
                    } else {
                        dto.setOutdoor(Boolean.parseBoolean(data));
                    }
                    dto.setStep(4);
                    placeSession.save(chatId, dto);
                    handlePlaces(absSender, chatId, dto);
                    cleanupSession(chatId, userId);
                }
                default -> {
                    sendError(absSender, chatId, ErrorConstants.UNKNOWN_STEP);
                    cleanupSession(chatId, userId);
                }
            }
        } catch (Exception e) {
            log.error("Error processing callback in get_place", e);
            sendError(absSender, chatId, ErrorConstants.ERROR_HAPPENED);
        }
    }

    private boolean needsOutdoorStep(String type) {
        return !(type.equals("SPORT_GROUND") || type.equals("GYM") ||
                type.equals("SWIMMING_POOL") || type.equals("MARTIAL_ARTS_HALL"));
    }

    private void showStepMenu(AbsSender absSender, Long chatId, PlaceDto dto, Long userId) {
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("getting");
        try {
            switch (dto.getStep()) {
                case 1 -> answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboard(answer));
                case 2 -> answer.setReplyMarkup(dto.getDistrict().buildSubdistrictsKeyboard(answer));
                case 3 -> answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createPlaceTypeKeyboard(answer));
                case 4 -> answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createOutdoorKeyboard(answer));
                default -> handleUnknownStep(chatId, userId, answer);
            }
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error showing step menu", e);
        }
    }

    private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
        answer.setText(ErrorConstants.UNKNOWN_STEP);
        cleanupSession(chatId, userId);
    }

    private void handlePlaces(AbsSender absSender, Long chatId, PlaceDto dto) {
        List<Place> places = new ArrayList<>();

        if (dto.getDistrict().equals(District.ALL_DISTRICTS) && dto.getOutdoor() == null) {
            places = placeService.findAllByPlaceType(dto.getPlaceType());
        }

        if (dto.getDistrict().equals(District.ALL_DISTRICTS) && dto.getOutdoor() != null) {
            places = placeService.findAllByPlaceTypeAndOutdoor(dto.getPlaceType(), dto.getOutdoor());
        }

        if (!dto.getDistrict().equals(District.ALL_DISTRICTS) && dto.getSubdistrict() != null && !dto.getSubdistrict().equals(Subdistrict.ALL_SUBDISTRICTS) && dto.getOutdoor() != null) {
            places = placeService.findByDistrictAndSubdistrictAndPlaceTypeAndOutdoor(dto.getDistrict(), dto.getSubdistrict(), dto.getPlaceType(), dto.getOutdoor());
        }

        if (!dto.getDistrict().equals(District.ALL_DISTRICTS) && dto.getSubdistrict() != null && !dto.getSubdistrict().equals(Subdistrict.ALL_SUBDISTRICTS) && dto.getOutdoor() == null) {
            places = placeService.findByDistrictAndSubdistrictAndPlaceType(dto.getDistrict(), dto.getSubdistrict(), dto.getPlaceType());
        }

        if (!dto.getDistrict().equals(District.ALL_DISTRICTS) && (dto.getSubdistrict() == null || dto.getSubdistrict().equals(Subdistrict.ALL_SUBDISTRICTS)) && dto.getOutdoor() != null) {
            places = placeService.findByDistrictAndPlaceTypeAndOutdoor(dto.getDistrict(), dto.getPlaceType(), dto.getOutdoor());
        }

        if (!dto.getDistrict().equals(District.ALL_DISTRICTS) && (dto.getSubdistrict() == null || dto.getSubdistrict().equals(Subdistrict.ALL_SUBDISTRICTS)) && dto.getOutdoor() == null) {
            places = placeService.findByDistrictAndPlaceType(dto.getDistrict(), dto.getPlaceType());
        }

        if (places.isEmpty()) {
            sendText(absSender, chatId, "По выбранным параметрам места не найдены \uD83E\uDD37\u200D♂\uFE0F");
            return;
        }

        for (Place place : places) {
            sendPlaceInfo(absSender, chatId, place);
        }
    }

    private void sendPlaceInfo(AbsSender absSender, Long chatId, Place place) {
        byte[] photo = place.getPhoto();
        String caption = createCaption(place);
        if (photo != null && photo.length > 0) {
            try (InputStream photoStream = new ByteArrayInputStream(photo)) {
                SendPhoto photoMessage = new SendPhoto();
                photoMessage.setChatId(chatId.toString());
                photoMessage.setPhoto(new InputFile(photoStream, "place.jpg"));
                photoMessage.setCaption(caption);
                photoMessage.setParseMode("Markdown");
                absSender.execute(photoMessage);
            } catch (TelegramApiException | IOException e) {
                log.error("Failed to send photo for place {}", place.getName(), e);
                sendText(absSender, chatId, caption);
            }
        } else {
            sendText(absSender, chatId, caption);
        }
    }

    private String createCaption(Place place) {
        String caption = String.format(
                "\uD83D\uDD39 Название: %s\n📍 Адрес: %s\n📝 Описание: %s",
                place.getName(),
                place.getAddress(),
                place.getDescription() != null
                        ? place.getDescription()
                        : "Описание не указано \uD83E\uDD37\u200D♂\uFE0F"
        );
        if (place.getWebSite() != null && !place.getWebSite().equals("-")) {
            caption += String.format("\n🌐 Ссылка: %s", place.getWebSite());
        }
        if (!place.getCoordinates().equals("-")) {
            try {
                String[] coordinates = place.getCoordinates().split(",");
                float latitude = Float.parseFloat(coordinates[0].trim());
                float longitude = Float.parseFloat(coordinates[1].trim());
                String mapLink = String.format("https://maps.google.com/?q=%f,%f", latitude, longitude);
                caption += String.format("\n🗺️ [Посмотреть местоположение в Google maps](%s)", mapLink);
            } catch (Exception e) {
                log.error("Failed to parse coordinates for place {}", place.getName(), e);
                caption += "\n\uD83E\uDDED Координаты: " + place.getCoordinates();
            }
        } else {
            caption += "\n\uD83E\uDDED Координаты отсутствуют \uD83E\uDD37\u200D♂\uFE0F";
        }
        return caption;
    }

    private void cleanupSession(Long chatId, Long userId) {
        placeSession.clear(chatId);
        commandStateStore.clearCurrentCommand(userId);
    }

    private void sendError(AbsSender absSender, Long chatId, String msg) {
        sendText(absSender, chatId, msg);
    }

    private void sendText(AbsSender absSender, Long chatId, String text) {
        try {
            absSender.execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }
}