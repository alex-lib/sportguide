package com.sport.service.bot;
import com.sport.service.bot.commands.admin.DeletePlaceCommand;
import com.sport.service.bot.commands.subscriber.GetPlaceCommand;
import com.sport.service.bot.commands.admin.CreatePlaceCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Scope("singleton")
public class SportPlacesAndEventsBot extends TelegramLongPollingCommandBot {

    public static final Map<Long, String> userCommandSessions = new ConcurrentHashMap<>();
    private static final Map<Long, List<String>> userSelections = new ConcurrentHashMap<>();

    private final String botUsername;
    private final CreatePlaceCommand createPlaceCommand;
    private final GetPlaceCommand getPlaceCommand;
    private final DeletePlaceCommand deletePlaceCommand;

    public SportPlacesAndEventsBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList,
            CreatePlaceCommand createPlaceCommand,
            GetPlaceCommand getPlaceCommand,
            DeletePlaceCommand deletePlaceCommand) {

        super(botToken);
        this.botUsername = botUsername;
        this.createPlaceCommand = createPlaceCommand;
        this.getPlaceCommand = getPlaceCommand;
        this.deletePlaceCommand = deletePlaceCommand;

        commandList.forEach(this::register);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNonCommandUpdate(Update update) {

        if (update.hasCallbackQuery()) {
            CallbackQuery callback = update.getCallbackQuery();
            if (callback == null || callback.getFrom() == null) return;

            long userId = callback.getFrom().getId();
            log.info("Callback from user: {}", userId);
            String data = callback.getData();
            log.info("Callback data: {}", data);

            if (!(callback.getMessage() instanceof Message)) return;
            Message message = (Message) callback.getMessage();

            String currentCommand = userCommandSessions.get(userId);
            log.info("currentCommand " + currentCommand);

            if ("get_place".equals(currentCommand)) {
                List<String> args = userSelections.computeIfAbsent(userId, k -> new ArrayList<>());
                args.add(data);
                getPlaceCommand.processMessage(this, message, args.toArray(new String[0]));
                if (args.size() >= 3) {
                    userSelections.remove(userId);
                    userCommandSessions.remove(userId);
                }
            } else if ("create_place".equals(currentCommand)) {
                createPlaceCommand.processCallback(this, callback);
                return;
            }
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long userId = message.getFrom().getId();
            String currentCommand = userCommandSessions.get(userId);

            if ("create_place".equals(currentCommand)) {
                createPlaceCommand.processTextInput(this, message);
                return;
            }

            if ("delete_place".equals(currentCommand)) {
                deletePlaceCommand.processTextInput(this, message);
                return;
            }
        }

        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            Message message = update.getMessage();
            long userId = message.getFrom().getId();
            String currentCommand = userCommandSessions.get(userId);

            if ("create_place".equals(currentCommand)) {
                createPlaceCommand.processPhotoInput(this, message);
            }
        }
    }
}