package com.sport.service.bot.commands.interfaces;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface CallbackProcessable {

    void processCallback(AbsSender absSender, CallbackQuery callback);
}