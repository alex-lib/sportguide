package com.sport.service.bot.commands.menu;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AdminStartMenu {

    private final SendMessage answer;

    private static final String ADMIN_MENU = """
               👋\s
               Ты здесь один из администраторов нашего бота.
               Ты можешь создать/удалить спортивные места и события.
               Также ты можешь пользоваться ботом как обычный юзер.
               Доступные команды:
               /get_place - выбрать место (по дефолту ты можешь найти сейчас одно место загруженное в БД: центральный-футбольное поле-помещение)
               /get_upcoming_events - получить список грядущих событий
               /get_notifications - подписаться на получение уведомлений о грядущих спортивных событиях
               /stop_notifications - отписаться от получения уведомлений о грядущих спортивных событиях
               /create_place - создать место
               /delete_place - удалить место
               /create_event - создать событие
               /delete_event - удалить событие
               \s""";

    public void getAdminMenu() {
        answer.setText(ADMIN_MENU);

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
        row3.add("/create_place");
        row3.add("/delete_place");
        keyboard.add(row3);

        KeyboardRow row4 = new KeyboardRow();
        row4.add("/create_event");
        row4.add("/delete_event");
        keyboard.add(row4);

        keyboardMarkup.setKeyboard(keyboard);
        answer.setReplyMarkup(keyboardMarkup);
    }
}