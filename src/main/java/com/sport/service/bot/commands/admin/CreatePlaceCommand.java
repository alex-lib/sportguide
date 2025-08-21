package com.sport.service.bot.commands.admin;
import com.sport.service.memory.CreatePlaceSession;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.*;
import com.sport.service.services.PlaceAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceDistrict;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import static com.sport.service.bot.SportPlacesAndEventsBot.userCommandSessions;

@RequiredArgsConstructor
@Service
@Slf4j
public class CreatePlaceCommand implements IBotCommand {

    private final CreatePlaceSession createPlaceSession;

    private final PlaceAdminService placeAdminService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getCommandIdentifier() {
        return "create_place";
    }

    @Override
    public String getDescription() {
        return "Let admin create a new place";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        log.info("Call command create_place by user: {}", user.getUserName());
        Long chatId = message.getChatId();

        PlaceDto dto = createPlaceSession.createSession(chatId);
        dto.setStep(1);

        userCommandSessions.put(user.getId(), "create_place");

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("Выберите район:");
        answer.setReplyMarkup(createDistrictKeyboard());

        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error sending initial message", e);
        }
    }

    public void processCallback(AbsSender absSender, CallbackQuery callback) {
        Long chatId = callback.getMessage().getChatId();
        Long userId = callback.getFrom().getId();

        if (!"create_place".equals(userCommandSessions.get(userId))) {
            log.warn("User {} not in create_place session", userId);
            return;
        }

        PlaceDto dto = createPlaceSession.getIfExists(chatId);
        if (dto == null) {
            log.warn("No session found for chatId: {}", chatId);
            sendErrorMessage(absSender, chatId, "Сессия истекла. Начните заново с /create_place");
            userCommandSessions.remove(userId);
            return;
        }

        String data = callback.getData();
        log.info("Processing callback for create_place: step={}, data={}", dto.getStep(), data);

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        try {
            switch (dto.getStep()) {
                case 1 -> handleDistrictStep(dto, data, answer);
                case 2 -> handleTypeStep(dto, data, answer);
                case 3 -> handleOutdoorStep(dto, data, answer);
                default -> handleUnknownStep(chatId, userId, answer);
            }

            absSender.execute(answer);

        } catch (Exception e) {
            log.error("Error processing callback", e);
            sendErrorMessage(absSender, chatId, "Произошла ошибка. Попробуйте еще раз.");
        }
    }

    private void handleDistrictStep(PlaceDto dto, String data, SendMessage answer) {
        try {
            dto.setDistrict(PlaceDistrict.valueOf(data));
            answer.setText("Выберите тип места:");
            answer.setReplyMarkup(createTypeKeyboard());
            dto.setStep(2);
        } catch (IllegalArgumentException e) {
            answer.setText("Неверный район. Попробуйте еще раз:");
            answer.setReplyMarkup(createDistrictKeyboard());
        }
    }

    private void handleTypeStep(PlaceDto dto, String data, SendMessage answer) {
        try {
            dto.setType(PlaceType.valueOf(data));
            answer.setText("Это улица или помещение?");
            answer.setReplyMarkup(createOutdoorKeyboard());
            dto.setStep(3);
        } catch (IllegalArgumentException e) {
            answer.setText("Неверный тип. Попробуйте еще раз:");
            answer.setReplyMarkup(createTypeKeyboard());
        }
    }

    private void handleOutdoorStep(PlaceDto dto, String data, SendMessage answer) {
        dto.setOutdoor(Boolean.parseBoolean(data));
        answer.setText("Введите название места:");
        dto.setStep(4);
    }

    private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
        answer.setText("Неизвестный шаг. Начните заново с /create_place");
        createPlaceSession.clear(chatId);
        userCommandSessions.remove(userId);
    }

    public void processTextInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        if (!"create_place".equals(userCommandSessions.get(userId))) {
            return;
        }

        PlaceDto dto = createPlaceSession.getIfExists(chatId);
        if (dto == null) {
            sendErrorMessage(absSender, chatId, "Сессия истекла. Начните заново с /create_place");
            userCommandSessions.remove(userId);
            return;
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        try {
            handleTextInput(message, dto, answer);
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error processing text input", e);
            sendErrorMessage(absSender, chatId, "Ошибка при обработке ввода. Попробуйте еще раз.");
        }
    }

    private void handleTextInput(Message message, PlaceDto dto, SendMessage answer) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        String text = message.getText();

        switch (dto.getStep()) {
            case 4 -> {
                dto.setName(text);
                answer.setText("Введите адрес:");
                dto.setStep(5);
            }
            case 5 -> {
                dto.setAddress(text);
                answer.setText("Введите описание:");
                dto.setStep(6);
            }
            case 6 -> {
                dto.setDescription(text);
                answer.setText("Введите сайт (или '-' если нет):");
                dto.setStep(7);
            }
            case 7 -> {
                dto.setWebSite(text);
                answer.setText("Отправьте фото:");
                dto.setStep(8);
            }
            case 8 -> {
                answer.setText("Пожалуйста, отправьте фото:");
            }
            default -> {
                answer.setText("Неизвестный шаг. Начните заново с /create_place");
                createPlaceSession.clear(chatId);
                userCommandSessions.remove(userId);
            }
        }
    }

    public void processPhotoInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        if (!"create_place".equals(userCommandSessions.get(userId))) {
            return;
        }

        PlaceDto dto = createPlaceSession.getIfExists(chatId);
        if (dto == null || dto.getStep() != 8) {
            sendErrorMessage(absSender, chatId, "Неожиданное фото. Начните заново с /create_place");
            userCommandSessions.remove(userId);
            return;
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        try {
            if (message.hasPhoto()) {
                String fileId = message.getPhoto().get(0).getFileId();
                byte[] photoBytes = downloadPhoto(absSender, fileId);
                dto.setPhoto(photoBytes);

                Place place = Place.builder()
                        .district(dto.getDistrict())
                        .type(dto.getType())
                        .outdoor(dto.getOutdoor())
                        .name(dto.getName())
                        .address(dto.getAddress())
                        .description(dto.getDescription())
                        .webSite(dto.getWebSite())
                        .photo(dto.getPhoto())
                        .build();

                placeAdminService.createPlace(place);
                answer.setText("✅ Место создано!");

                createPlaceSession.clear(chatId);
                userCommandSessions.remove(userId);
            } else {
                answer.setText("Пожалуйста, отправьте фото:");
            }

            absSender.execute(answer);

        } catch (Exception e) {
            log.error("Error processing photo", e);
            sendErrorMessage(absSender, chatId, "❌ Ошибка при создании места: " + e.getMessage());
        }
    }

    private void sendErrorMessage(AbsSender absSender, Long chatId, String message) {
        try {
            SendMessage errorMsg = new SendMessage();
            errorMsg.setChatId(chatId.toString());
            errorMsg.setText(message);
            absSender.execute(errorMsg);
        } catch (Exception e) {
            log.error("Error sending error message", e);
        }
    }

    private byte[] downloadPhoto(AbsSender absSender, String fileId) throws Exception {
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = absSender.execute(getFileMethod);

        String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + file.getFilePath();

        try (InputStream inputStream = new URL(fileUrl).openStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    private InlineKeyboardMarkup createOutdoorKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Улица", "true"),
                createButtonRow("Помещение", "false")
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
                createButtonRow("Теннисный корт", "TENNIS_COURT"),
                createButtonRow("Пинг-понг стол", "PINGPONG_TABLE"),
                createButtonRow("Падел корт", "PADEL_COURT"),
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