package com.sport.service.bot;

import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.interfaces.PhotoProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.mappers.ButtonToCommandMapper;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SportGuideBot extends TelegramLongPollingCommandBot {
    private final Map<String, IBotCommand> commands = new HashMap<>();
    private final CommandStateStore commandStateStore;
    private final String botUsername;

    public SportGuideBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList,
            CommandStateStore commandStateStore) {
        super(botToken);
        this.botUsername = botUsername;
        this.commandStateStore = commandStateStore;
        commandList.forEach(this::registerCommand);
    }

    private void registerCommand(IBotCommand command) {
        commands.put("/" + command.getCommandIdentifier(), command);
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
            if (callback.getFrom().getIsBot()) return;
            if (!(callback.getMessage() instanceof Message)) return;

            long userId = callback.getFrom().getId();
            log.info("Callback from user: {}", userId);
            deleteMenuAndMessage((Message) callback.getMessage());

            String currentCommand = commandStateStore.getCurrentCommand(userId);
            IBotCommand command = commands.get("/" + currentCommand);

            if (command instanceof CallbackProcessable callbackProcessor) {
                callbackProcessor.processCallback(this, update.getCallbackQuery());
                return;
            }
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long userId = message.getFrom().getId();
            String text = message.getText();
            String mappedCommand = ButtonToCommandMapper.mapButtonToCommand(text);
            if (mappedCommand != null) {
                commandStateStore.clearCurrentCommand(userId);
                dispatchCommand(mappedCommand, update);
                return;
            }

            String currentCommand = commandStateStore.getCurrentCommand(userId);
            IBotCommand command = commands.get("/" + currentCommand);

            if (command instanceof TextProcessable textProcessor) {
                textProcessor.processTextInput(this, update.getMessage());
                return;
            }
        }

        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            Message message = update.getMessage();
            long userId = message.getFrom().getId();
            String currentCommand = commandStateStore.getCurrentCommand(userId);
            IBotCommand command = commands.get("/" + currentCommand);

            if (command instanceof PhotoProcessable photoProcessor) {
                photoProcessor.processPhotoInput(this, update.getMessage());
            }
        }
    }

    private void deleteMenuAndMessage(Message message) {
        try {
            InlineKeyboardMarkup loadingKeyboard = InlineKeyboardMarkup.builder()
                    .keyboard(List.of(List.of(InlineKeyboardButton.builder()
                                    .text("⏳ Удаление меню...")
                                    .callbackData("IGNORE")
                            .build()))).build();

            EditMessageReplyMarkup loadingMarkup = new EditMessageReplyMarkup();
            loadingMarkup.setChatId(message.getChatId());
            loadingMarkup.setMessageId(message.getMessageId());
            loadingMarkup.setReplyMarkup(loadingKeyboard);
            execute(loadingMarkup);

            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                try {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(message.getChatId());
                    deleteMessage.setMessageId(message.getMessageId());
                    execute(deleteMessage);
                    //log.info("The message @{}@ has been deleted from chat with id: {}", message.getText(), message.getChatId());
                } catch (TelegramApiException e) {
                    log.error("The message @{}@ hasn't been deleted from chat with id - {}: {}",
                            message.getText(), message.getChatId(), e.getMessage());
                }
            }, 30, TimeUnit.SECONDS);
        } catch (TelegramApiException e) {
            log.error("Error occurred while deleting the message", e);
        }
    }

    private void dispatchCommand(String commandText, Update update) {
        IBotCommand command = commands.get(commandText);
        if (command != null) {
            command.processMessage(this, update.getMessage(), new String[0]);
        } else {
            log.warn("Unknown command: {}", commandText);
        }
    }
}