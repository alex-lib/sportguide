package com.sport.service.bot.commands.subscriber;
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
public class SupportProjectCommand implements IBotCommand {

    @Override
    public String getCommandIdentifier() {
        return "support_project";
    }

    @Override
    public String getDescription() {
        return "Give to subscriber info to support project";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        log.info("Call command support_project by user: {}",user.getUserName());

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        answer.setText("""
                \s
                USDT
                address:
                network:
                \s""");
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /support_project command", e);
        }
    }
}