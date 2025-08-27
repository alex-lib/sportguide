package com.sport.service.bot.commands.subscriber;
import com.sport.service.entities.subscriber.Subscriber;
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
public class StopNotificationsCommand implements IBotCommand {

    private final SubscriberService subscriberService;

    @Override
    public String getCommandIdentifier() {
        return "stop_notifications";
    }

    @Override
    public String getDescription() {
        return "Change variable getEvents to false";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {


        User user = message.getFrom();
        log.info("Call command stop_notifications by user: {}", user.getUserName());

        // 🚀 Ignore bot’s own messages immediately
        if (user.getIsBot()) {
            return;
        }

        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        Subscriber subscriber = subscriberService.findById(userId);

        if (subscriber.getGetEvents().equals(Boolean.FALSE)) {
            answer.setText("Вы не подписаны на получение уведомлений");
        } else {
            subscriber.setGetEvents(Boolean.FALSE);
            subscriberService.updateSubscriber(subscriber, user.getId());
            answer.setText("Теперь вы не будете получать уведомление при создании события");
        }
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /get_upcoming_events command", e);
        }
    }
}