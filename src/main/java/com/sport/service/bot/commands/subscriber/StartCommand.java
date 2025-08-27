package com.sport.service.bot.commands.subscriber;
import com.sport.service.bot.commands.menu.AdminStartMenu;
import com.sport.service.bot.commands.menu.SubscriberStartMenu;
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
@RequiredArgsConstructor
@Slf4j
public class StartCommand implements IBotCommand {

    private final SubscriberService subscriberService;

    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Launch bot and save user's data to database or find there uploaded admin's data";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        log.info("Call command start by user: {}, with id: {}",user.getUserName(), user.getId());

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        if (subscriberService.checkIfAdmin(user.getId())) {
            AdminStartMenu adminStartMenu = new AdminStartMenu(answer);
            adminStartMenu.getAdminMenu();
        } else {
            subscriberService.addSubscriber(user);
            SubscriberStartMenu subscriberStartMenu = new SubscriberStartMenu(answer);
            subscriberStartMenu.getSubscriberMenu();
        }

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
    }
}