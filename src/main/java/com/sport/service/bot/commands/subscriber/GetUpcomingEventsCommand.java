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
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command get_upcoming_events by userId={}, username={}", userId, user.getUserName());
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId);
        List<Event> events = eventService.findAll();
        try {
            if (events.isEmpty()) {
                answer.setText("Ближайших событий нет \uD83E\uDD37\u200D♂\uFE0F");
                absSender.execute(answer);
            } else {
                for (Event event : events) {
                    answer.setText(createEventMessage(event));
                    absSender.execute(answer);
                }
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred in /get_upcoming_events command", e);
        }
    }

    private String createEventMessage(Event event){
        return "✨ Событие: " + event.getName() + "\n" +
                "\uD83D\uDCDD Описание: " + event.getDescription() + "\n" +
                "\uD83D\uDCC5 Дата: " + event.getDate() + "\n" +
                "⌚\uFE0F Время: " + event.getTime() + "\n" +
                "\uD83D\uDD17 Ссылка: " + event.getLink() + "\n" +
                "\uD83D\uDCCD Место: " + event.getPlaceName() + "\n" +
                "\uD83D\uDDFA Район: " + event.getDistrict() + "\n" +
                "\uD83D\uDCEE Адрес: " + event.getAddress();
    }
}