package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.constants.CommandsConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class SupportProjectCommand implements IBotCommand {

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.SUPPORT_PROJECT;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.SUPPORT_PROJECT_DESCRIPTION;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long userId = user.getId();
        Long chatId = message.getChatId();
        log.info("Call command support_project by userId={}, username={}", userId, user.getUserName());
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId);
        answer.setText(CommandsConstants.SUPPORT_PROJECT_TEXT);
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /support_project command", e);
        }
    }
}