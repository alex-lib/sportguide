package com.sport.service.bot.commands.menu;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SubscriberStartMenu {

    private final SendMessage answer;

    private static final String SUBSCRIBER_MENU = """
            👋\s
            Доступные команды:
            /get_place - выбрать место
            /get_upcoming_events - получить список грядущих событий
            /get_notifications - подписаться на получение уведомлений о грядущих спортивных событиях
            /stop_notifications - отписаться от получения уведомлений о грядущих спортивных событиях
            /support_project - поддержать проект
            /contact_admin - предложить админу создать новое место, скорректировать имеющееся место или создать грядущее спортивное событие
            \s""";

    public void getSubscriberMenu() {
        answer.setText(SUBSCRIBER_MENU);

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
    }
}