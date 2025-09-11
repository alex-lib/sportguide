package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.Type;
import com.sport.service.services.PlaceService;
import com.sport.service.sessions.CommandStateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetPlaceCommand implements IBotCommand {

    @Autowired
    private PlaceService placeService;

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
        log.info("Call command get_place by user: {}", user.getUserName());

        commandStateStore.setCurrentCommand(user.getId(), "get_place");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());

        try {
            if (arguments.length == 0) {
                sendMessage.setText("Выберите район Воронежа в котором хотите найти место:");
                sendMessage.setReplyMarkup(ChoosingPlaceOptionsMenu.createDistrictKeyboardForGettingPlace());
                absSender.execute(sendMessage);


            } else if (arguments.length == 1) {
                sendMessage.setText("Выберите тип локации:");
                sendMessage.setReplyMarkup(ChoosingPlaceOptionsMenu.createTypeKeyboard());
                absSender.execute(sendMessage);


            } else if (arguments.length == 2) {

                if (!arguments[1].equals("SPORT_GROUND")
                        && !arguments[1].equals("SWIMMING_POOL")
                        && !arguments[1].equals("MARTIAL_ARTS_HALL")
                        && !arguments[1].equals("GYM")) {
                    sendMessage.setText("Выберите тип локации (улица/помещение):");
                    sendMessage.setReplyMarkup(ChoosingPlaceOptionsMenu.createOutdoorKeyboardForGettingPlace());
                    absSender.execute(sendMessage);


                } else {
                    handlePlaces(absSender, message, arguments[0], arguments[1], "all");
                }

            } else if (arguments.length == 3) {
                String district = arguments[0];
                String type = arguments[1];
                String outdoor = arguments[2];
                handlePlaces(absSender, message, district, type, outdoor);

            } else {
                log.warn("Unexpected number of arguments: {}", Arrays.toString(arguments));
                sendMessage.setText("Неверный формат команды. Попробуйте снова.");
                absSender.execute(sendMessage);
            }
        } catch (Exception e) {
            log.error("Error while processing get_place command", e);
            try {
                sendMessage.setText("Произошла ошибка при обработке запроса. Попробуйте снова позже.");
                absSender.execute(sendMessage);
            } catch (TelegramApiException ex) {
                log.error("Failed to send error message to user", ex);
            }
        }
    }

    private void handlePlaces(AbsSender absSender, Message message,
                              String districtArg, String typeArg, String outdoorArg) {
        List<Place> places = placeService.findByDistrict(District.valueOf(districtArg));
        places = placeService.findByType(places, Type.valueOf(typeArg));

        if ("true".equals(outdoorArg) || "false".equals(outdoorArg)) {
            places = placeService.findByOutdoor(places, Boolean.parseBoolean(outdoorArg));
        }

        if (places.isEmpty()) {
            try {
                absSender.execute(new SendMessage(message.getChatId().toString(), "По выбранным параметрам места не найдены."));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (Place place : places) {
                byte[] photo = place.getPhoto();

                if (photo != null) {
                    try (InputStream photoStream = new ByteArrayInputStream(photo)) {
                        SendPhoto photoMessage = new SendPhoto();
                        photoMessage.setChatId(message.getChatId());
                        photoMessage.setPhoto(new InputFile(photoStream, "photo.jpg"));
                        photoMessage.setCaption(new StringBuilder()
                                .append(place.getName()).append("\n")
                                .append(place.getAddress()).append("\n")
                                .append(place.getDescription()).append("\n")
                                .append(place.getWebSite())
                                .toString()); // Add caption for better UX
                        absSender.execute(photoMessage);
                        log.info("Sent photo for place '{}': {} bytes", place.getName(), photo.length);
                    } catch (TelegramApiException e) {
                        log.error("Failed to send photo for place '{}': {}", place.getName(), e.getMessage());
                        SendMessage fallbackMsg = new SendMessage();
                        fallbackMsg.setChatId(message.getChatId());
                        fallbackMsg.setText("Фото недоступно для места: " + place.getName());
                        try {
                            absSender.execute(fallbackMsg);
                        } catch (TelegramApiException ex) {
                            throw new RuntimeException(ex);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}