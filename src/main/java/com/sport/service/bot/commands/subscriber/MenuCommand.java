package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.menu.AdminMenu;
import com.sport.service.bot.commands.menu.SubscriberMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuCommand implements IBotCommand {
    private final SubscriberService subscriberService;

    private final TelegramMessageSender sender;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.MENU;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.MENU_DESCRIPTION;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command menu by userId={}, username={}", userId, user.getUserName());

        try {
            SendMessage answer = new SendMessage();
            answer.setChatId(chatId);

            if (subscriberService.checkIfAdmin(userId)) {
                AdminMenu adminMenu = new AdminMenu(answer);
                adminMenu.getAdminMenu();
            } else {
                subscriberService.addSubscriber(user);
                SubscriberMenu subscriberMenu = new SubscriberMenu(answer);
                subscriberMenu.getSubscriberMenu();
            }
            answer.setText(CommandsConstants.MENU_MESSAGE);
            sender.sendMessageWithoutPhoto(answer);
        } catch (Exception e) {
            log.error("Error occurred in /menu command", e);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ERROR_HAPPENED);
        }
    }
}