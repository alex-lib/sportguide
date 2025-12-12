package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.redis_store.commands_store.sessions.PlaceSession;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetPlaceCommand implements IBotCommand, CallbackProcessable {
    private final CommandStateStore commandStateStore;
    private final PlaceSession placeSession;

    private final PlaceService placeService;

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
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
            commandStateStore.clearCurrentCommand(userId);
            return;
        }

        String data = callback.getData();
        log.info("Processing callback for get_place: step={}, data={}", dto.getStep(), data);

        if (KeyboardConstants.BACK.equals(data)) { //user wants to back the previous menu to reconsider his choice
            switch (dto.getStep()) {
                case 2 -> {
                    dto.setPlaceType(null);
                    dto.setStep(1);
                    placeSession.save(chatId, dto);
                    showStepMenu(absSender, chatId, dto, userId);
                }
                case 3 -> {
                    if (needsOutdoorStep(dto.getPlaceType().name())) {
                        dto.setOutdoor(null);
                        dto.setStep(2);
                        placeSession.save(chatId, dto);
                        showStepMenu(absSender, chatId, dto, userId);
                    } else {
                        dto.setPlaceType(null);
                        dto.setStep(1);
                        placeSession.save(chatId, dto);
                        showStepMenu(absSender, chatId, dto, userId);
                    }
                }
                case 4 -> {
                    dto.setDistrict(null);
                    dto.setStep(3);
                    placeSession.save(chatId, dto);
                    showStepMenu(absSender, chatId, dto, userId);
                }
            }
        }

        try {
            switch (dto.getStep()) {
                case 1 -> {
                    if (!data.equals(KeyboardConstants.BACK)) {
                        dto.setPlaceType(PlaceType.valueOf(data));
                        if (needsOutdoorStep(data)) {
                            dto.setStep(2);
                            placeSession.save(chatId, dto);
                            showStepMenu(absSender, chatId, dto, userId);
                        } else {
                            dto.setStep(3);
                            dto.setOutdoor(null);
                            placeSession.save(chatId, dto);
                            showStepMenu(absSender, chatId, dto, userId);
                        }
                    }
                }
                case 2 -> {
                    if (!data.equals(KeyboardConstants.BACK)) {
                        if (data.equals(KeyboardConstants.NULL)) {
                            dto.setOutdoor(null);
                        } else {
                            dto.setOutdoor(Boolean.parseBoolean(data));
                        }
                        dto.setStep(3);
                        placeSession.save(chatId, dto);
                        showStepMenu(absSender, chatId, dto, userId);
                    }
                }
                case 3 -> {
                    if (!data.equals(KeyboardConstants.BACK)) {
                        District district = District.valueOf(data);
                        dto.setDistrict(district);
                        if (dto.getPlaceType() == PlaceType.SPORT_GROUND
                                && !dto.getDistrict().equals(District.ALL_DISTRICTS)
                                && !dto.getDistrict().equals(District.LENINSKYY)) {
                            dto.setStep(4);
                            placeSession.save(chatId, dto);
                            showStepMenu(absSender, chatId, dto, userId);
                        } else {
                            dto.setSubdistrict(null);
                            placeSession.save(chatId, dto);
                            handlePlaces(chatId, dto);
                            cleanupSession(chatId, userId);
                        }
                    }
                }
                case 4 -> {
                    if (!data.equals(KeyboardConstants.BACK)) {
                        dto.setSubdistrict(SubDistrict.valueOf(data));
                        placeSession.save(chatId, dto);
                        handlePlaces(chatId, dto);
                        cleanupSession(chatId, userId);
                    }
                }
                default -> {
                    sender.sendMessageWithoutPhoto(chatId, ErrorConstants.UNKNOWN_STEP);
                    cleanupSession(chatId, userId);
                }
            }
        } catch (Exception e) {
            log.error("Error processing callback in get_place", e);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
        }
    }

    private boolean needsOutdoorStep(String type) {
        return !(type.equals(PlaceType.SPORT_GROUND.name()) || type.equals(PlaceType.GYM.name()) ||
                type.equals(PlaceType.SWIMMING_POOL.name()) || type.equals(PlaceType.MARTIAL_ARTS_HALL.name()));
    }

    private void showStepMenu(AbsSender absSender, Long chatId, PlaceDto dto, Long userId) {
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("getting");
        try {
            switch (dto.getStep()) {
                case 1 -> answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createPlaceTypeKeyboard(answer));
                case 2 -> answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createOutdoorKeyboard(answer));
                case 3 -> answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboard(answer));
                case 4 -> answer.setReplyMarkup(dto.getDistrict().buildSubdistrictsKeyboard(answer));
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

    private void handlePlaces(Long chatId, PlaceDto dto) {
        List<Place> places = new ArrayList<>();

        if (dto.getDistrict().equals(District.ALL_DISTRICTS) && dto.getOutdoor() == null) {
            places = placeService.findAllByPlaceType(dto.getPlaceType());
        }

        if (dto.getDistrict().equals(District.ALL_DISTRICTS) && dto.getOutdoor() != null) {
            places = placeService.findAllByPlaceTypeAndOutdoor(dto.getPlaceType(), dto.getOutdoor());
        }

        if (!dto.getDistrict().equals(District.ALL_DISTRICTS) && dto.getSubdistrict() != null && !dto.getSubdistrict().equals(SubDistrict.ALL_SUBDISTRICTS) && dto.getOutdoor() != null) {
            places = placeService.findByDistrictAndSubdistrictAndPlaceTypeAndOutdoor(dto.getDistrict(), dto.getSubdistrict(), dto.getPlaceType(), dto.getOutdoor());
        }

        if (!dto.getDistrict().equals(District.ALL_DISTRICTS) && dto.getSubdistrict() != null && !dto.getSubdistrict().equals(SubDistrict.ALL_SUBDISTRICTS) && dto.getOutdoor() == null) {
            places = placeService.findByDistrictAndSubdistrictAndPlaceType(dto.getDistrict(), dto.getSubdistrict(), dto.getPlaceType());
        }

        if (!dto.getDistrict().equals(District.ALL_DISTRICTS) && (dto.getSubdistrict() == null || dto.getSubdistrict().equals(SubDistrict.ALL_SUBDISTRICTS)) && dto.getOutdoor() != null) {
            places = placeService.findByDistrictAndPlaceTypeAndOutdoor(dto.getDistrict(), dto.getPlaceType(), dto.getOutdoor());
        }

        if (!dto.getDistrict().equals(District.ALL_DISTRICTS) && (dto.getSubdistrict() == null || dto.getSubdistrict().equals(SubDistrict.ALL_SUBDISTRICTS)) && dto.getOutdoor() == null) {
            places = placeService.findByDistrictAndPlaceType(dto.getDistrict(), dto.getPlaceType());
        }

        if (places.isEmpty()) {
            sender.sendMessageWithoutPhoto(chatId, "По выбранным параметрам места не найдены \uD83E\uDD37\u200D♂\uFE0F");
            return;
        }

        for (Place place : places) {
            sendPlaceInfo(chatId, place);
        }
    }

    private void sendPlaceInfo(Long chatId, Place place) {
        byte[] photo = place.getPhoto();
        String caption = createCaption(place);
        if (photo != null && photo.length > 0) {
            sender.sendMessageWithPhoto(chatId, photo, caption);
        } else {
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
        }
    }

    private String createCaption(Place place) {
        String caption = String.format(
                "\uD83D\uDD39 <b>Название:</b> %s\n📍 <b>Адрес:</b> %s\n📝 <b>Описание:</b> %s",
                place.getName(),
                place.getAddress(),
                place.getDescription() != null
                        ? place.getDescription()
                        : "Описание не указано \uD83E\uDD37\u200D♂\uFE0F"
        );
        if (place.getWebSite() != null && !place.getWebSite().equals("-")) {
            caption += String.format("\n🌐 <b>Ссылка:</b> %s", place.getWebSite());
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
                caption += "\n\uD83E\uDDED <b>Координаты:</b> " + place.getCoordinates();
                caption += "\n <i>#место</i>";
            }
        } else {
            caption += "\n\uD83E\uDDED Координаты отсутствуют \uD83E\uDD37\u200D♂\uFE0F";
            caption += "\n <i>#место</i>";
        }
        return caption;
    }

    private void cleanupSession(Long chatId, Long userId) {
        placeSession.clear(chatId);
        commandStateStore.clearCurrentCommand(userId);
    }
}