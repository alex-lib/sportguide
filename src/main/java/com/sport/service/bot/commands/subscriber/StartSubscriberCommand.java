package com.sport.service.bot.commands.subscriber;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartSubscriberCommand implements IBotCommand {

    private final SubscriberService subscriberService;

    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Launch bot and save user's data to database";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        User user = message.getFrom();

        log.info("Call command start by user: {}", user.getUserName());
        subscriberService.addSubscriber(user);
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());
        answer.setText("""
               👋\s
               Доступные команды:
               /get_place - выбрать место
               /get_upcoming_events - получить список грядущих событий
               /get_notifications - подписаться на получение уведомлений о грядущих спортивных событиях
               /stop_notifications - отписаться от получения уведомлений о грядущих спортивных событиях
               /support_project - поддержать проект
               /contact_admin - предложить админу создать новое место, скорректировать имеющееся место или создать грядущее спортивное событие
               \s""");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("/get_place");
        row1.add("/get_upcoming_events");
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("/get_notifications");
        row2.add("/stop_notifications");
        keyboard.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add("/support_project");
        row3.add("/contact_admin");
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        answer.setReplyMarkup(keyboardMarkup);

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
    }
}