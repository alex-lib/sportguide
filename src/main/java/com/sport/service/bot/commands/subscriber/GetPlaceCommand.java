package com.sport.service.bot.commands.subscriber;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceDistrict;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.services.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import static com.sport.service.bot.SportPlacesAndEventsBot.userCommandSessions;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetPlaceCommand implements IBotCommand {

    @Autowired
    @Qualifier("placeServiceImpl")
    private  PlaceService placeService;

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

        userCommandSessions.put(user.getId(), "get_place");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());

        try {
            if (arguments.length == 0) {

                sendMessage.setText("Выберите район Воронежа в котором хотите найти место:");
                sendMessage.setReplyMarkup(createDistrictKeyboard());
                absSender.execute(sendMessage);

            } else if (arguments.length == 1) {

                sendMessage.setText("Выберите тип локации:");
                sendMessage.setReplyMarkup(createTypeKeyboard());
                absSender.execute(sendMessage);

            } else if (arguments.length == 2) {

                sendMessage.setText("Выберите тип локации (улица/помещение):");
                sendMessage.setReplyMarkup(createOutdoorKeyboard());
                absSender.execute(sendMessage);

            } else if (arguments.length == 3) {

                List<Place> places = placeService.findByPlaceDistrict(PlaceDistrict.valueOf(arguments[0]));
                places = placeService.findByPlaceType(places, PlaceType.valueOf(arguments[1]));
                places = placeService.findByOutdoor(places, Boolean.valueOf(arguments[2]));

                if (places.isEmpty()) {
                    sendMessage.setText("По выбранным параметрам места не найдены.");
                    absSender.execute(sendMessage);
                } else {

                    for (Place place : places) {
                        sendMessage.setText(place.getName() + "\n" +
                                place.getAddress() + "\n" +
                                place.getDescription() + "\n" +
                                place.getWebSite());

                        absSender.execute(sendMessage);

                        byte[] photo = place.getPhoto();

                        if (photo != null) {

                            try (InputStream photoStream = new ByteArrayInputStream(photo)) {
                                SendPhoto photoMessage = new SendPhoto();
                                photoMessage.setChatId(message.getChatId()); // <-- сюда нужно ID чата
                                photoMessage.setPhoto(new InputFile(photoStream, "photo.png"));
                                absSender.execute(photoMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            } catch(Exception e){
                e.printStackTrace();
            }
        }


    private ReplyKeyboard createOutdoorKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Улица", "TRUE"),
                createButtonRow("Помещение", "FALSE")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardMarkup createDistrictKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Железнодорожный", "ZHELEZNODOROZHNYY"),
                createButtonRow("Центральный", "CENTRALNYY"),
                createButtonRow("Коминтерновский", "KOMINTERNOVSKYY"),
                createButtonRow("Ленинский", "LENINSKYY"),
                createButtonRow("Советский", "SOVETSKYY"),
                createButtonRow("Левобережный", "LEVOBEREZNYY"),
                createButtonRow("За городом", "BEHIND_OF_CITY"),
                createButtonRow("Поиск по всем районам", "ALL_DISTRICTS")
        );

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardMarkup createTypeKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Уличная спортивная площадка", "SPORT_GROUND"),
                createButtonRow("Футбольное поле", "FOOTBALL_FIELD"),
                createButtonRow("Баскетбольное поле", "BASKETBALL_FIELD"),
                createButtonRow("Волейбольное поле", "VOLLEYBALL_FIELD"),
                createButtonRow("Теннисный корт", "TENNIS_CORT"),
                createButtonRow("Пинг-понг стол", "PINGPONG_CORT"),
                createButtonRow("Падел корт", "PADEL_CORT"),
                createButtonRow("Ледовая арена", "ICE_RING"),
                createButtonRow("Бассейн", "SWIMMING_POOL"),
                createButtonRow("Беговая зона", "RUNNING_PLACE")
        );

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private List<InlineKeyboardButton> createButtonRow(String text, String callbackData) {
        return List.of(InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build());
    }
}