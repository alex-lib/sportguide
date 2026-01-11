package com.sport.service.bot;

import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.interfaces.PhotoProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.mappers.ButtonToCommandMapper;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.redis_store.commands_store.sessions.JointTrainingRejectingSession;
import com.sport.service.services.JointTrainingService;
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

    private final JointTrainingService jointTrainingService;
    private final JointTrainingRejectingSession jointTrainingRejectingSession;
    private final TelegramMessageSender sender;

    @Value("${telegram.mainAdminId}")
    private String adminId;

    @Value("${telegram.secondAdminId}")
    private String secondAdminId;

    public SportGuideBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList,
            CommandStateStore commandStateStore,
            JointTrainingService jointTrainingService,
            JointTrainingRejectingSession jointTrainingRejectingSession,
            TelegramMessageSender sender) {
        super(botToken);
        this.botUsername = botUsername;
        this.commandStateStore = commandStateStore;
        this.jointTrainingService = jointTrainingService;
        this.jointTrainingRejectingSession = jointTrainingRejectingSession;
        this.sender = sender;
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
            String data = callback.getData();

            log.info("Callback from user: {}, data: {}", userId, data);

            if (data.startsWith("APPROVE_JT:")) {
                Long id = Long.valueOf(data.replace("APPROVE_JT:", ""));
                jointTrainingService.approveJointTraining(id);
                sender.sendMessageWithoutPhoto(Long.valueOf(secondAdminId),"✔️ Одобрено");
                return;
            }

            if (data.startsWith("REJECT_JT:")) {
                Long jtId = Long.valueOf(data.replace("REJECT_JT:", ""));
                jointTrainingRejectingSession.start(userId, jtId);
                sender.sendMessageWithoutPhoto(userId,
                        "❌ Вы собираетесь отклонить тренировку #" + jtId +
                                "\nВведите причину отказа текстом:");
                return;
            }

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

            if (jointTrainingRejectingSession.isWaiting(Long.valueOf(secondAdminId))) {
                var session = jointTrainingRejectingSession.get(Long.valueOf(secondAdminId));
                Long jtId = session.getJointTrainingId();
                jointTrainingService.rejectJointTraining(jtId, text);
                sender.sendMessageWithoutPhoto(Long.valueOf(secondAdminId),
                        "❌ Заявка #" + jtId + " отклонена.\nПричина отправлена пользователю:\n" + text);
                jointTrainingRejectingSession.clear(Long.valueOf(secondAdminId));
                return;
            }

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
                                    .text(KeyboardConstants.DELETING_MENU)
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