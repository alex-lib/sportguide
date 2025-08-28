package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.commands.menu.AdminMenu;
import com.sport.service.bot.commands.menu.SubscriberMenu;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MenuCommand implements IBotCommand {

    private final SubscriberService subscriberService;

    @Override
    public String getCommandIdentifier() {
        return "menu";
    }

    @Override
    public String getDescription() {
        return "Show user menu with buttons";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        log.info("Call command menu by user: {}, with id: {}",user.getUserName(), user.getId());

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        if (subscriberService.checkIfAdmin(user.getId())) {
            AdminMenu adminMenu = new AdminMenu(answer);
            adminMenu.getAdminMenu();
        } else {
            subscriberService.addSubscriber(user);
            SubscriberMenu subscriberMenu = new SubscriberMenu(answer);
            subscriberMenu.getSubscriberMenu();
        }

        answer.setText("Меню с кнопками представлены ниже.");

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
    }
}
