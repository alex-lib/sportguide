package com.sport.service.bot.commands.menu;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AdminMenu {

    private final SendMessage answer;

    public static final String ADMIN_MENU = """
               \s
               Ты админ бота и можешь создавать/удалять спортивные места/события.
               Также ты можешь пользоваться ботом как обычный юзер.
               
               Доступные команды:
            📍 Выбрать место - выбрать место
               📅 Ближайшие события - получить список грядущих событий
            🔔 Подписаться на уведомления - подписаться на получение уведомлений о спортивных событиях
            🚫 Отписаться от уведомлений - отписаться от получения уведомлений о спортивных событиях
               ➕ Создать место - создать место
               ❌ Удалить место - удалить место
               ➕ Создать событие - создать событие
               ❌ Удалить событие - удалить событие
               🔎 Получить кол-во юзеров - получить кол-во юзеров стартанувших бот
            🔎 Получить кол-во подписок - получить кол-во подписок на получение событий
            ✉\uFE0F Отправить сообщение всем пользователям - ну ты все понял:)
               \s""";

    public void getAdminMenu() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("📍 Выбрать место");
        row1.add("📅 Ближайшие события");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🔔 Подписаться на уведомления");
        row2.add("🚫 Отписаться от уведомлений");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("➕ Создать место");
        row3.add("❌ Удалить место");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("➕ Создать событие");
        row4.add("❌ Удалить событие");

        KeyboardRow row5 = new KeyboardRow();
        row5.add("🔎 Получить кол-во юзеров");
        row5.add("🔎 Получить кол-во подписавшихся юзеров");

        KeyboardRow row6 = new KeyboardRow();
        row6.add("✉\uFE0F Отправить сообщение всем пользователям");

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);
        keyboard.add(row6);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        keyboardMarkup.setKeyboard(keyboard);
        answer.setReplyMarkup(keyboardMarkup);
    }
}