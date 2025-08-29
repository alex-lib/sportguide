package com.sport.service.bot.commands.admin;
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
public class GetUsersCountCommand implements IBotCommand {

    private final SubscriberService subscriberService;

    @Override
    public String getCommandIdentifier() {
        return "get_users_count";
    }

    @Override
    public String getDescription() {
        return "Let admin get count of users";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());
        User user = message.getFrom();

        if (subscriberService.checkIfAdmin(user.getId())) {
            answer.setText("Количество юзеров: " + subscriberService.getUsersCount());
        } else {
            answer.setText("Вы не являетесь администратором.");
        }
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /get_users_count command", e);
        }
    }
}