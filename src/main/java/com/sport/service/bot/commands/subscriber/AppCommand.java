package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.constants.CommandsConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;

@Service
@Slf4j
public class AppCommand implements IBotCommand {

    @Value("${webapp.url:https://sportguide.online}")
    private String webappUrl;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.APP;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.APP_DESCRIPTION;
    }

    @Override
    public void processMessage(
            AbsSender absSender,
            Message message,
            String[] arguments
    ) {
        Long userId = message.getFrom().getId();

        log.info("Call command app by userId={}", userId);

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        KeyboardButton webAppButton = KeyboardButton.builder()
                .text("🚀 Sportguide")
                .webApp(new WebAppInfo(webappUrl))
                .build();

        KeyboardRow row = new KeyboardRow();
        row.add(webAppButton);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        keyboard.setKeyboard(List.of(row));

        answer.setReplyMarkup(keyboard);

        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error occurred in /app command", e);
        }
    }
}