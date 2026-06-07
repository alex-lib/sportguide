package com.sport.service.bot.commands.menu;

import com.sport.service.bot.constants.MenuConstants;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SubscriberMenu {
    private final SendMessage answer;

    public void getSubscriberMenu() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(MenuConstants.CHOOSE_PLACE);
        row1.add(MenuConstants.UPCOMING_EVENTS);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(MenuConstants.SUBSCRIBE_TO_NOTIFICATIONS);
        row2.add(MenuConstants.UNSUBSCRIBE_TO_NOTIFICATIONS);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(MenuConstants.SUPPORT_PROJECT);
        row3.add(MenuConstants.CONTACT_ADMIN);

        KeyboardRow row4 = new KeyboardRow();
        row4.add(MenuConstants.CREATE_TRAINING_PROGRAM);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setKeyboard(keyboard);
        answer.setReplyMarkup(replyKeyboardMarkup);
    }
}