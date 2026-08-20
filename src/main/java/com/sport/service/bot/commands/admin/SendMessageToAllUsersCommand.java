package com.sport.service.bot.commands.admin;

import com.sport.service.annotations.AdminOnly;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.UtilMethods;
import com.sport.service.bot.commands.interfaces.PhotoProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.dto.MessageDto;
import com.sport.service.entities.Subscriber;
import com.sport.service.store.commands.CommandStateStore;
import com.sport.service.store.commands.sessions.MessageSession;
import com.sport.service.services.NotificationSenderService;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendMessageToAllUsersCommand implements IBotCommand, TextProcessable, PhotoProcessable {
    private final CommandStateStore commandStateStore;
    private final MessageSession messageSession;

    private final SubscriberService subscriberService;
    private final NotificationSenderService notificationSenderService;

    private final TelegramMessageSender sender;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.SEND_MESSAGE_TO_ALL_USERS;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.SEND_MESSAGE_TO_ALL_USERS_DESCRIPTION;
    }

    @Override
    @AdminOnly
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command send_message_to_all_users by userId={}, username={}", userId, user.getUserName());

        try {
            MessageDto dto = messageSession.createSession(chatId);
            dto.setStep(1);
            messageSession.save(chatId, dto);
            commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
            sender.sendMessageWithoutPhoto(chatId, CommandsConstants.ENTER_TEXT_TO_SEND_TO_ALL_USERS);
        } catch (Exception e) {
            log.error("Error occurred in /send_message_to_all_users command", e);
            messageSession.clear(chatId);
            commandStateStore.clearCurrentCommand(userId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
        }
    }

    private void handleTextInput(Message message, MessageDto dto, SendMessage answer) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        String text = message.getText();

        List<Long> subscriberIds = subscriberService.findAll()
                .stream()
                .map(Subscriber::getId)
                .toList();

        switch (dto.getStep()) {
            case 1 -> {
                dto.setMessage(text);
                answer.setText(CommandsConstants.SEND_PHOTO_TO_SEND_TO_ALL_USERS);
                dto.setStep(2);
            }
            case 2 -> {
                if (CommandsConstants.NO_PHOTO.equals(text)) {
                    notificationSenderService.sendAdminToSubscriberNotification(dto.getMessage(), null, subscriberIds);
                    log.info("Publishing EventSendMessageToAllUsers: message='{}'", dto.getMessage());
                    answer.setText(CommandsConstants.MESSAGE_SENT_TO_ALL_USERS);
                    messageSession.clear(chatId);
                    commandStateStore.clearCurrentCommand(userId);
                } else {
                    answer.setText(CommandsConstants.SEND_PHOTO_TO_SEND_TO_ALL_USERS_2);
                }
            }
            default -> handleUnknownStep(chatId, userId, answer);
        }
    }

    @Override
    public void processTextInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();

        MessageDto dto = messageSession.getIfExists(chatId);
        if (!ifSessionValid(chatId, dto)) {
            return;
        }

        try {
            SendMessage answer = new SendMessage();
            answer.setChatId(chatId.toString());
            handleTextInput(message, dto, answer);
            messageSession.save(chatId, dto);

            if (!answer.getText().isEmpty()) {
                sender.sendMessageWithoutPhoto(answer);
            }
        } catch (Exception e) {
            log.error("Error processing text input", e);
            messageSession.clear(chatId);
            commandStateStore.clearCurrentCommand(chatId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
        }
    }

    @Override
    public void processPhotoInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        MessageDto dto = messageSession.getIfExists(chatId);
        if (!ifSessionValid(chatId, dto)) {
            return;
        }

        List<Long> subscriberIds = subscriberService.findAll()
                .stream()
                .map(Subscriber::getId)
                .toList();

        try {
            if (message.hasPhoto()) {
                List<PhotoSize> photos = message.getPhoto();
                PhotoSize bestPhoto = photos.get(photos.size() - 1);
                String fileId = bestPhoto.getFileId();
                log.info("Processing photo: {} sizes available, selected size: {}x{}, file size: {} bytes",
                        photos.size(), bestPhoto.getWidth(), bestPhoto.getHeight(), bestPhoto.getFileSize());
                byte[] photoBytes = UtilMethods.downloadPhoto(absSender, fileId, botToken);
                log.info("Downloaded photo: {} bytes", photoBytes.length);

                dto.setPhoto(photoBytes);
                notificationSenderService.sendAdminToSubscriberNotification(dto.getMessage(), dto.getPhoto(), subscriberIds);
                messageSession.clear(chatId);
                commandStateStore.clearCurrentCommand(userId);
                sender.sendMessageWithoutPhoto(chatId, CommandsConstants.MESSAGE_SENT);
            } else {
                sender.sendMessageWithoutPhoto(chatId, CommandsConstants.SEND_PHOTO_TO_SEND_TO_ALL_USERS_2);
            }
        } catch (Exception e) {
            log.error("Error processing photo", e);
            messageSession.clear(chatId);
            commandStateStore.clearCurrentCommand(userId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.UNEXPECTED_PHOTO);
        }
    }

    private void handleUnknownStep(Long chatId, Long userId, SendMessage answer) {
        answer.setText(ErrorConstants.UNKNOWN_STEP);
        messageSession.clear(chatId);
        commandStateStore.clearCurrentCommand(userId);
    }

    private boolean ifSessionValid(Long chatId, MessageDto dto) {
        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(chatId))) {
            log.warn("User {} is not in send_message_to_all_users session", chatId);
            return false;
        }

        if (dto == null) {
            log.warn("No session found for chatId: {}", chatId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
            commandStateStore.clearCurrentCommand(chatId);
            return false;
        }
        return true;
    }
}