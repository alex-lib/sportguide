package com.sport.service.bot.commands.menu;

import com.sport.service.bot.constants.MenuConstants;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AdminMenu {
    private final SendMessage answer;

    public static final String ADMIN_MENU = MenuConstants.ADMIN_MENU;

    public void getAdminMenu() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(MenuConstants.CHOOSE_PLACE);
        row1.add(MenuConstants.UPCOMING_EVENTS);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(MenuConstants.SUBSCRIBE_TO_NOTIFICATIONS);
        row2.add(MenuConstants.UNSUBSCRIBE_TO_NOTIFICATIONS);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(MenuConstants.CREATE_PLACE);
        row3.add(MenuConstants.DELETE_PLACE);

        KeyboardRow row4 = new KeyboardRow();
        row4.add(MenuConstants.CREATE_EVENT);
        row4.add(MenuConstants.DELETE_EVENT);

        KeyboardRow row5 = new KeyboardRow();
        row5.add(MenuConstants.SEND_MESSAGE_TO_ALL_USERS);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        keyboardMarkup.setKeyboard(keyboard);
        answer.setReplyMarkup(keyboardMarkup);
    }
}