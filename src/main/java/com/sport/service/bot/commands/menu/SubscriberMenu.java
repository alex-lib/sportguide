package com.sport.service.bot.commands.menu;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SubscriberMenu {

    private final SendMessage answer;

    public static final String SUBSCRIBER_MENU = """
            \s
            Доступные команды:
            📍 Выбрать место - выбрать место
            📅 Ближайшие события - получить список грядущих событий
            🔔 Подписаться на уведомления - подписаться на получение уведомлений о грядущих спортивных событиях
            🚫 Отписаться от уведомлений - отписаться от получения уведомлений о грядущих спортивных событиях
            \uD83C\uDFCB\uFE0F\u200D♂\uFE0F Поддержать проект - поддержать проект
            \uD83E\uDE83 Связаться с админом - предложить админу создать новое место, скорректировать имеющееся место или создать грядущее спортивное событие
            \s""";

    public void getSubscriberMenu() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("📍 Выбрать место");
        row1.add("📅 Ближайшие события");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🔔 Подписаться на уведомления");
        row2.add("🚫 Отписаться от уведомлений");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("\uD83C\uDFCB\uFE0F\u200D♂\uFE0F Поддержать проект");
        row3.add("\uD83E\uDE83 Связаться с админом");

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setKeyboard(keyboard);
        answer.setReplyMarkup(replyKeyboardMarkup);
    }
}