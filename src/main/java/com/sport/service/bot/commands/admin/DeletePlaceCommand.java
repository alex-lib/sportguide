package com.sport.service.bot.commands.admin;
import com.sport.service.memory.CreatePlaceSession;
import com.sport.service.services.PlaceAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import static com.sport.service.bot.SportPlacesAndEventsBot.userCommandSessions;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeletePlaceCommand implements IBotCommand {

    private final PlaceAdminService placeAdminService;

    private final CreatePlaceSession createPlaceSession;

    @Override
    public String getCommandIdentifier() {
        return "delete_place";
    }

    @Override
    public String getDescription() {
        return "Let admin to delete a created place";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        log.info("Call command delete_place by user: {}", user.getUserName());

        Long chatId = message.getChatId();

        userCommandSessions.put(user.getId(), "delete_place");

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId);

        answer.setText("Удалить место можно только по точному имени ранее сохраненного места. " +
                "Напишите название места, которое хотите удалить:");

        String text = message.getText();
        log.info("Received text: {}", text);


        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error sending initial message", e);
        }
    }

    public void processTextInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        if (!"delete_place".equals(userCommandSessions.get(userId))) {
            return;
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());
        String text = message.getText();
        log.info("Received text: {}", text);
        placeAdminService.deletePlaceByName(text);
        createPlaceSession.clear(chatId);
        userCommandSessions.remove(userId);
        answer.setText("Удаление места завершено.");

        try {
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error processing text input", e);
            sendErrorMessage(absSender, chatId, "Ошибка при обработке ввода. Попробуйте еще раз.");
        }
    }

    private void sendErrorMessage(AbsSender absSender, Long chatId, String message) {
        try {
            SendMessage errorMsg = new SendMessage();
            errorMsg.setChatId(chatId.toString());
            errorMsg.setText(message);
            absSender.execute(errorMsg);
        } catch (Exception e) {
            log.error("Error sending error message", e);
        }
    }

}