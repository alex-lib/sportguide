package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.services.PlaceService;
import com.sport.service.sessions.CommandStateStore;
import com.sport.service.sessions.PlaceSession;
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
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetPlaceCommand implements IBotCommand, CallbackProcessable {
    private final String sessionExpired = "Сессия истекла. Начните заново \uD83D\uDD04";
    private final String unknownStep = "Неизвестный шаг. Начните заново \uD83D\uDD04";

    private final PlaceSession placeSession;
    private final PlaceService placeService;

    private final CommandStateStore commandStateStore;

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
        commandStateStore.setCurrentCommand(userId, "get_place");
        showStepMenu(absSender, chatId, dto, userId);
    }

    public void processCallback(AbsSender absSender, CallbackQuery callback) {
        Long chatId = callback.getMessage().getChatId();
        Long userId = callback.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        if (!"get_place".equals(commandStateStore.getCurrentCommand(userId))) {
            log.warn("User {} not in get_place session", userId);
            return;
        }

        PlaceDto dto = placeSession.getIfExists(chatId);
        if (dto == null) {
            sendError(absSender, chatId, sessionExpired);
            commandStateStore.clearCurrentCommand(userId);
            return;
        }

        String data = callback.getData();
        log.info("Processing callback for get_place: step={}, data={}", dto.getStep(), data);

        if ("BACK".equals(data)) {
            if (dto.getStep() == 2) {
                dto.setPlaceType(null);
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
                case 1 -> {
                    dto.setDistrict(District.valueOf(data));
                    dto.setStep(2);
                    placeSession.save(chatId, dto);
                    showStepMenu(absSender, chatId, dto, userId);
                }
                case 2 -> {
                    dto.setPlaceType(PlaceType.valueOf(data));
                    if (needsOutdoorStep(data)) {
                        dto.setStep(3);
                        placeSession.save(chatId, dto);
                        showStepMenu(absSender, chatId, dto, userId);
                    } else {
                        dto.setStep(4);
                        dto.setOutdoor(null);
                        placeSession.save(chatId, dto);
                        handlePlaces(absSender, chatId, dto);
                        cleanupSession(chatId, userId);
                    }
                }
                case 3 -> {
                    if (data.equals("null")) {
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
                    sendError(absSender, chatId, unknownStep);
                    cleanupSession(chatId, userId);
                }
            }
        } catch (Exception e) {
            log.error("Error processing callback in get_place", e);
            sendError(absSender, chatId, "Ошибка. Попробуйте снова \uD83D\uDD04");
        }
    }

    private boolean needsOutdoorStep(String type) {
        return !(type.equals("SPORT_GROUND") || type.equals("GYM") ||
                type.equals("SWIMMING_POOL") || type.equals("MARTIAL_ARTS_HALL"));
    }

    private void showStepMenu(AbsSender absSender, Long chatId, PlaceDto dto, Long userId) {
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());
        try {
            switch (dto.getStep()) {
                case 1 -> answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboardForGetting(answer));
                case 2 -> answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createTypeKeyboard(answer));
                case 3 -> answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createOutdoorKeyboardForGettingPlace(answer));
                default -> handleUnknownStep(chatId, userId, answer);
            }
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error showing step menu", e);
        }
    }

    private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
        answer.setText(unknownStep);
        cleanupSession(chatId, userId);
    }

    private void handlePlaces(AbsSender absSender, Long chatId, PlaceDto dto) {
        List<Place> places = dto.getOutdoor() != null ?
                placeService.findByDistrictAndPlaceTypeAndOutdoor(dto.getDistrict(), dto.getPlaceType(), dto.getOutdoor())
                : placeService.findByDistrictAndPlaceType(dto.getDistrict(), dto.getPlaceType());

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
            } catch (Exception e) {
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
        sendText(absSender, chatId, "⚠️ " + msg);
    }

    private void sendText(AbsSender absSender, Long chatId, String text) {
        try {
            absSender.execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }
}