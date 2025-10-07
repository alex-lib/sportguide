package com.sport.service.bot.commands.interfaces;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface TextProcessable {

    void processTextInput(AbsSender absSender, Message message);
}