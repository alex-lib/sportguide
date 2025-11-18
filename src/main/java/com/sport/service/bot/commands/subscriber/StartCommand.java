package com.sport.service.bot.commands.subscriber;
import com.sport.service.bot.commands.menu.AdminMenu;
import com.sport.service.bot.commands.menu.SubscriberMenu;
import com.sport.service.bot.constants.CommandsConstants;
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
        return CommandsConstants.START;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.START_DESCRIPTION;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command start by userId={}, username={}", userId, user.getUserName());
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId);

        if (subscriberService.checkIfAdmin(userId)) {
            answer.setText(AdminMenu.ADMIN_MENU);
            AdminMenu adminMenu = new AdminMenu(answer);
            adminMenu.getAdminMenu();
        } else {
            subscriberService.addSubscriber(user);
            answer.setText(SubscriberMenu.SUBSCRIBER_MENU);
            SubscriberMenu subscriberMenu = new SubscriberMenu(answer);
            subscriberMenu.getSubscriberMenu();
        }
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
    }
}