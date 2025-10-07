package com.sport.service.bot;

import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.interfaces.PhotoProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.entities.Event;
import com.sport.service.entities.subscriber.Subscriber;
import com.sport.service.events.EventContactAdmin;
import com.sport.service.events.EventCreatedEvent;
import com.sport.service.events.EventSendMessageToAllUsers;
import com.sport.service.mappers.ButtonToCommandMapper;
import com.sport.service.sessions.CommandStateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SportGuideBot extends TelegramLongPollingCommandBot {
    private final CommandStateStore commandStateStore;
    private final String botUsername;
    private final Map<String, IBotCommand> commands = new HashMap<>();

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

    @Value("${telegram.mainAdminId}")
    private String mainAdminId;

    private void registerCommand(IBotCommand command) {
        commands.put(command.getCommandIdentifier(), command);
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

            long userId = callback.getFrom().getId();
            log.info("Callback from user: {}", userId);

            if (!(callback.getMessage() instanceof Message)) return;

            deleteMenuAndMessage((Message) callback.getMessage());

            String currentCommand = commandStateStore.getCurrentCommand(userId);
            IBotCommand handler = commands.get(currentCommand);

            if (handler instanceof CallbackProcessable callbackHandler) {
                callbackHandler.processCallback(this, update.getCallbackQuery());
                return;
            }
//            if ("get_place".equals(currentCommand)) {
//                getPlaceCommand.processCallback(this, callback);
//                return;
//            } else if ("create_place".equals(currentCommand)) {
//                createPlaceCommand.processCallback(this, callback);
//                return;
//            } else if ("create_event".equals(currentCommand)) {
//                createEventCommand.processCallback(this, callback);
//                return;
//            }
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
            IBotCommand handler = commands.get(currentCommand);

            if (handler instanceof TextProcessable textHandler) {
                textHandler.processTextInput(this, update.getMessage());
                return;
            }
//            if ("create_place".equals(currentCommand)) {
//                createPlaceCommand.processTextInput(this, message);
//                return;
//            }
//
//            if ("delete_place".equals(currentCommand)) {
//                deletePlaceCommand.processTextInput(this, message);
//                return;
//            }
//
//            if ("create_event".equals(currentCommand)) {
//                createEventCommand.processTextInput(this, message);
//                return;
//            }
//
//            if ("delete_event".equals(currentCommand)) {
//                deleteEventCommand.processTextInput(this, message);
//                return;
//            }
//
//            if ("contact_admin".equals(currentCommand)) {
//                contactAdminCommand.processTextInput(this, message);
//                return;
//            }
//
//            if ("send_message_to_all_users".equals(currentCommand)) {
//                sendMessageToAllUsersCommand.processTextInput(this, message);
//                return;
//            }
        }

        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            Message message = update.getMessage();
            long userId = message.getFrom().getId();

            String currentCommand = commandStateStore.getCurrentCommand(userId);
            IBotCommand handler = commands.get(currentCommand);

            if (handler instanceof PhotoProcessable photoHandler) {
                photoHandler.processPhotoInput(this, update.getMessage());
            }
//            if ("create_place".equals(currentCommand)) {
//                createPlaceCommand.processPhotoInput(this, message);
//            }
//
//            if ("send_message_to_all_users".equals(currentCommand)) {
//                sendMessageToAllUsersCommand.processPhotoInput(this, message);
//            }
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
                    log.info("The message has been deleted: {}", message.getMessageId());
                } catch (TelegramApiException e) {
                    log.error("The message hasn't been deleted {} with id - {}: {}",
                            message.getText(), message.getMessageId(), e.getMessage());
                }
            }, 30, TimeUnit.SECONDS);
        } catch (TelegramApiException e) {
            log.error("Error occurred while deleting the message", e);
        }
    }

    @EventListener
    private void sendNotification(EventCreatedEvent event) {
        try {
            String notification = createEventNotification(event.getEvent());
            for (Subscriber subscriber : event.getSubscribers()) {
                SendMessage sendMessage = SendMessage.builder()
                        .chatId(subscriber.getId().toString())
                        .text(notification)
                        .build();
                execute(sendMessage);
            }
            log.info("Notification of event {} sent to subscribers", event.getEvent());
        } catch (TelegramApiException e) {
            log.error("Failed to send notification of {} to subscribers", event.getEvent());
        }
    }

    @EventListener
    private void sendMessageToAllUsers(EventSendMessageToAllUsers event) {
        try {
            String host = java.net.InetAddress.getLocalHost().getHostName();
            log.info("Listener invoked on host={} thread={} subs={}",
                    host, Thread.currentThread().getName(), event.getSubscribers().size());
            if (event.getPhoto() != null) {
                SendPhoto photoMessage = new SendPhoto();
                try (InputStream photoStream = new ByteArrayInputStream(event.getPhoto())) {
                    photoMessage.setPhoto(new InputFile(photoStream, "place.jpg"));
                    photoMessage.setCaption(event.getMessage());
                    photoMessage.setParseMode("Markdown");
                } catch (Exception e) {
                    log.error("Failed to send photo for place {}", event.getMessage(), e);
                }
                for (Subscriber subscriber : event.getSubscribers()) {
                    photoMessage.setChatId(subscriber.getId().toString());
                    execute(photoMessage);
                }
            } else {
                for (Subscriber subscriber : event.getSubscribers()) {
                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(subscriber.getId().toString())
                            .text(event.getMessage())
                            .build();
                    execute(sendMessage);
                }
            }
        } catch (TelegramApiException e) {
            log.error("Failed to send message to subscribers");
        } catch (Exception ignored) {
        }
    }

    private String createEventNotification(Event event) {
        return new StringBuilder()
                .append("✨ Событие: ").append(event.getName()).append("\n")
                .append("\uD83D\uDCDD Описание: ").append(event.getDescription()).append("\n")
                .append("\uD83D\uDCC5 Дата: ").append(event.getDate()).append("\n")
                .append("⌚\uFE0F Время: ").append(event.getTime()).append("\n")
                .append("\uD83D\uDD17 Ссылка: ").append(event.getLink()).append("\n")
                .append("\uD83D\uDCCD Место: ").append(event.getPlaceName()).append("\n")
                .append("\uD83D\uDDFA Район: ").append(event.getDistrict()).append("\n")
                .append("\uD83D\uDCEE Адрес: ").append(event.getAddress()).append("\n")
                .append("Спасибо за подписку \uD83D\uDE4F")
                .toString();
    }

    @EventListener
    private void sendNotificationToAdmin(EventContactAdmin event) {
        try {
            String notification = createNotificationForAdmin(event.getText(), event.getUser());
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(mainAdminId)
                    .text(notification)
                    .build();
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send notification of {} to admin", event.getText());
        }
    }

    private String createNotificationForAdmin(String text, User user) {
        return new StringBuilder()
                .append("\uD83D\uDC64 Пользователь: ")
                .append(user.getUserName())
                .append(" c id: ")
                .append(user.getId())
                .append(" написал вам:\n")
                .append(text)
                .toString();
    }

    private void dispatchCommand(String commandText, Update update) {
        String[] parts = commandText.split(" ");
        String command = parts[0];
        String[] args = parts.length > 1 ? parts[1].split(" ") : new String[0];
        IBotCommand cmd = commands.get(command);
        if (cmd != null) {
            try {
                cmd.processMessage(this, update.getMessage(), args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.warn("Unknown command: " + command);
        }
    }
}