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

    private String createEventMessage(Event event) {
        return new StringBuilder().append("✨ Событие: ").append(event.getName()).append("\n")
                .append("\uD83D\uDCDD Описание: ").append(event.getDescription()).append("\n")
                .append("\uD83D\uDCC5 Дата: ").append(event.getDate()).append("\n")
                .append("⌚️ Время: ").append(event.getTime()).append("\n")
                .append("\uD83D\uDD17 Ссылка: ").append(event.getLink()).append("\n")
                .append("\uD83D\uDCCD Место: ").append(event.getPlaceName()).append("\n")
                .append("\uD83D\uDDFA Район: ").append(event.getDistrict()).append("\n")
                .append("\uD83D\uDCEE Адрес: ").append(event.getAddress()).toString();
    }
}