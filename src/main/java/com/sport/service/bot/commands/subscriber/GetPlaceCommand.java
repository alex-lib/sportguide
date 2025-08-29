package com.sport.service.bot.commands.subscriber;
import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Type;
import com.sport.service.sessions.CommandStateStore;
import com.sport.service.services.PlaceService;
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
import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetPlaceCommand implements IBotCommand {

    @Autowired
    private  PlaceService placeService;

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
                sendMessage.setText("Выберите тип локации (улица/помещение):");
                sendMessage.setReplyMarkup(ChoosingPlaceOptionsMenu.createOutdoorKeyboardForGettingPlace());
                absSender.execute(sendMessage);

            } else if (arguments.length == 3) {
                List<Place> places = placeService.findByDistrict(District.valueOf(arguments[0]));

                places = placeService.findByType(places, Type.valueOf(arguments[1]));

                if (arguments[2].equals("false") || arguments[2].equals("true")) {
                    places = placeService.findByOutdoor(places, Boolean.valueOf(arguments[2]));
                }

                if (places.isEmpty()) {
                    sendMessage.setText("По выбранным параметрам места не найдены.");
                    absSender.execute(sendMessage);
                } else {

                    for (Place place : places) {
                        sendMessage.setText(new StringBuilder()
                                .append(place.getName()).append("\n")
                                .append(place.getAddress()).append("\n")
                                .append(place.getDescription()).append("\n")
                                .append(place.getWebSite())
                                .toString());

                        absSender.execute(sendMessage);
                        byte[] photo = place.getPhoto();

                        if (photo != null) {
                            try (InputStream photoStream = new ByteArrayInputStream(photo)) {
                                SendPhoto photoMessage = new SendPhoto();
                                photoMessage.setChatId(message.getChatId());
                                photoMessage.setPhoto(new InputFile(photoStream, "photo.jpg"));
                                photoMessage.setCaption(place.getName()); // Add caption for better UX
                                absSender.execute(photoMessage);
                                log.info("Sent photo for place '{}': {} bytes", place.getName(), photo.length);
                            } catch (TelegramApiException e) {
                                log.error("Failed to send photo for place '{}': {}", place.getName(), e.getMessage());
                                SendMessage fallbackMsg = new SendMessage();
                                fallbackMsg.setChatId(message.getChatId());
                                fallbackMsg.setText("Фото недоступно для места: " + place.getName());
                                absSender.execute(fallbackMsg);
                            }
                        }
                    }
                }
            }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
}