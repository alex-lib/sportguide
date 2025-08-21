package com.sport.service.bot.commands.admin;
import com.sport.service.dto.EventDto;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Event;
import com.sport.service.entities.place.Place;
import com.sport.service.memory.CreateEventSession;
import com.sport.service.memory.CreatePlaceSession;
import com.sport.service.services.PlaceAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static com.sport.service.bot.SportPlacesAndEventsBot.userCommandSessions;


@RequiredArgsConstructor
@Service
@Slf4j
public class CreateEventCommand implements IBotCommand {

    private final CreateEventSession createEventSession;

    private final PlaceAdminService placeAdminService;

    @Override
    public String getCommandIdentifier() {
        return "create_event";
    }

    @Override
    public String getDescription() {
        return "Let admin create a new event";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        log.info("Call command create_event by user: {}", user.getUserName());
        Long chatId = message.getChatId();

        EventDto dto = createEventSession.createSession(chatId);
        dto.setStep(1);

        userCommandSessions.put(user.getId(), "create_event");

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("Введите наименование события:");

        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error sending initial message", e);
        }
    }

    public void processTextInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        if (!"create_event".equals(userCommandSessions.get(userId))) {
            return;
        }

        EventDto dto = createEventSession.getIfExists(chatId);
        if (dto == null) {
            sendErrorMessage(absSender, chatId, "Сессия истекла. Начните заново с /create_event");
            userCommandSessions.remove(userId);
            return;
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        try {
            handleTextInput(message, dto, answer);

            Event event = Place.builder()
                    .district(dto.getDistrict())
                    .type(dto.getType())
                    .outdoor(dto.getOutdoor())
                    .name(dto.getName())
                    .address(dto.getAddress())
                    .description(dto.getDescription())
                    .webSite(dto.getWebSite())
                    .photo(dto.getPhoto())
                    .build();


            placeAdminService.createEvent(place);
            answer.setText("✅ Место создано!");

            createPlaceSession.clear(chatId);
            userCommandSessions.remove(userId);

            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error processing text input", e);
            sendErrorMessage(absSender, chatId, "Ошибка при обработке ввода. Попробуйте еще раз.");
        }
    }

    private void handleTextInput(Message message, EventDto dto, SendMessage answer) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        String text = message.getText();

        switch (dto.getStep()) {
            case 1 -> {
                dto.setName(text);
                answer.setText("Введите адрес:");
                dto.setStep(2);
            }
            case 2 -> {
                dto.setAddress(text);
                answer.setText("Введите описание:");
                dto.setStep(3);
            }
            case 3 -> {
                dto.setDescription(text);
                answer.setText("Введите ссылку на событие (или '-' если нет):");
                dto.setStep(4);
            }
            case 4 -> {
                dto.setLink(text);
                answer.setText("Введите имя места где будет организовано событие:");
                dto.setStep(5);
            }
            case 5 -> {
                dto.setPlaceName(text);
                answer.setText("Введите дату в формате YYYY-MM-DD:");
                dto.setStep(6);
            }
            case 6 -> {
                dto.setDate(text);
                answer.setText("Введите время в формате HH:mm:");
                dto.setStep(7);
            }
            case 7 -> {
                dto.setTime(text);
            }
            default -> {
                answer.setText("Неизвестный шаг. Начните заново с /create_place");
                createEventSession.clear(chatId);
                userCommandSessions.remove(userId);
            }
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
}
