package com.sport.service.bot.commands.subscriber;
import com.sport.service.entities.Event;
import com.sport.service.services.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetUpcomingEventsCommand implements IBotCommand {

    private final EventService eventService;

    @Override
    public String getCommandIdentifier() {
        return "get_upcoming_events";
    }

    @Override
    public String getDescription() {
        return "Let subscribers get list of upcoming events";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        log.info("Call command get_upcoming_events by user: {}", user.getUserName());

        List<Event> events = eventService.findAll();

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        try {
            for (Event event : events) {
                answer.setText(createEventMessage(event));
                absSender.execute(answer);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred in /get_upcoming_events command", e);
        }
    }

    private String createEventMessage(Event event){
        return "Событие: " + event.getName() + "\n" +
                "Описание: " + event.getDescription() + "\n" +
                "Дата: " + event.getDate() + "\n" +
                "Время: " + event.getTime() + "\n" +
                "Место: " + event.getPlaceName() + "\n" +
                "Ссылка: " + event.getLink() + "\n" +
                "Район: " + event.getDistrict() + "\n" +
                "Адресс: " + event.getAddress();
    }
}