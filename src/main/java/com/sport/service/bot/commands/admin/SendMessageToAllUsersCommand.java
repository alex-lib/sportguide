package com.sport.service.bot.commands.admin;

import com.sport.service.aop.annotations.AdminOnly;
import com.sport.service.bot.commands.interfaces.PhotoProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.dto.MessageDto;
import com.sport.service.entities.subscriber.Subscriber;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.redis_store.commands_store.sessions.MessageSession;
import com.sport.service.services.NotificationSenderService;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendMessageToAllUsersCommand implements IBotCommand, TextProcessable, PhotoProcessable {
    private final CommandStateStore commandStateStore;
    private final MessageSession messageSession;

    private final SubscriberService subscriberService;
    private final NotificationSenderService notificationSenderService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getCommandIdentifier() {
        return "send_message_to_all_users";
    }

    @Override
    public String getDescription() {
        return "Let admin send message to all users";
    }

    @Override
    @AdminOnly
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command send_message_to_all_users by userId={}, username={}", userId, user.getUserName());
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        MessageDto dto = messageSession.createSession(chatId);
        dto.setStep(1);
        messageSession.save(chatId, dto);
        commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
        answer.setText("📩 Напишите, что вы хотите отправить подписчикам:");
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /send_message_to_all_users command", e);
            sendErrorMessage(absSender, chatId, ErrorConstants.ENTERING_ERROR);
        }
    }

    private void handleTextInput(Message message, MessageDto dto, SendMessage answer, AbsSender absSender) {
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
                answer.setText("\uD83D\uDDBC Отправьте фото или картинку (или - если картинки/фото нет):");
                dto.setStep(2);
            }
            case 2 -> {
                if ("-".equals(text)) {
                    notificationSenderService.sendAdminToSubscriberNotification(dto.getMessage(), null, subscriberIds);
                    log.info("Publishing EventSendMessageToAllUsers: message='{}', subscribers={}", dto.getMessage(), subscriberService.findAll().size());
                    answer.setText("Сообщение отправлено всем пользователям ✅");
                    messageSession.clear(chatId);
                    commandStateStore.clearCurrentCommand(userId);
                } else {
                    answer.setText("\uD83D\uDDBC Пожалуйста, отправьте фото (или - если картинки/фото нет):");
                }
            }
            default -> {
                answer.setText(ErrorConstants.UNKNOWN_STEP);
                messageSession.clear(chatId);
                commandStateStore.clearCurrentCommand(userId);
            }
        }
    }

    @Override
    public void processTextInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) {
            return;
        }

        MessageDto dto = messageSession.getIfExists(chatId);
        if (dto == null) {
            sendErrorMessage(absSender, chatId, ErrorConstants.SESSION_EXPIRED);
            commandStateStore.clearCurrentCommand(userId);
            return;
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());
        try {
            handleTextInput(message, dto, answer, absSender);
            messageSession.save(chatId, dto);

            if (!answer.getText().isEmpty()) {
                absSender.execute(answer);
            }
        } catch (Exception e) {
            log.error("Error processing text input", e);
            sendErrorMessage(absSender, chatId, ErrorConstants.ENTERING_ERROR);
        }
    }

    @Override
    public void processPhotoInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) {
            return;
        }

        MessageDto dto = messageSession.getIfExists(chatId);
        if (dto == null || dto.getStep() != 2) {
            sendErrorMessage(absSender, chatId, ErrorConstants.SESSION_EXPIRED);
            commandStateStore.clearCurrentCommand(userId);
            return;
        }

        List<Long> subscriberIds = subscriberService.findAll()
                .stream()
                .map(Subscriber::getId)
                .toList();

        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());
        try {
            if (message.hasPhoto()) {
                List<PhotoSize> photos = message.getPhoto();
                PhotoSize bestPhoto = photos.get(photos.size() - 1);
                String fileId = bestPhoto.getFileId();
                log.info("Processing photo: {} sizes available, selected size: {}x{}, file size: {} bytes",
                        photos.size(), bestPhoto.getWidth(), bestPhoto.getHeight(), bestPhoto.getFileSize());
                byte[] photoBytes = downloadPhoto(absSender, fileId);
                log.info("Downloaded photo: {} bytes", photoBytes.length);
                dto.setPhoto(photoBytes);
                notificationSenderService.sendAdminToSubscriberNotification(dto.getMessage(), dto.getPhoto(), subscriberIds);
                answer.setText("Сообщение отправлено всем пользователям ✅");
                messageSession.clear(chatId);
                commandStateStore.clearCurrentCommand(userId);
            } else {
                answer.setText("\uD83D\uDDBC Пожалуйста, отправьте фото (или - если картинки/фото нет):");
            }
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error processing photo", e);
            sendErrorMessage(absSender, chatId, ErrorConstants.UNEXPECTED_PHOTO);
        }
    }

    private byte[] downloadPhoto(AbsSender absSender, String fileId) {
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(fileId);
        File file = null;
        try {
            file = absSender.execute(getFileMethod);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + file.getFilePath();
        log.info("Downloading photo from: {}", fileUrl);
        try (InputStream inputStream = new URL(fileUrl).openStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to download photo from URL: {}", fileUrl, e);
            throw new RuntimeException("Failed to download photo: " + e.getMessage(), e);
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