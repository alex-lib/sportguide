package com.sport.service.bot.commands.admin;

import com.sport.service.dto.MessageDto;
import com.sport.service.events.EventSendMessageToAllUsers;
import com.sport.service.services.SubscriberService;
import com.sport.service.sessions.CommandStateStore;
import com.sport.service.sessions.MessageSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
public class SendMessageToAllUsersCommand implements IBotCommand {

    private final ApplicationEventPublisher eventPublisher;

    private final CommandStateStore commandStateStore;

    private final SubscriberService subscriberService;

    private final MessageSession messageSession;

    private final String sessionExpired = "Сессия истекла. Начните заново \uD83D\uDD04";

    private final String unknownStep = "Неизвестный шаг. Начните заново \uD83D\uDD04";

    private final String errorToProcessInput = "Ошибка при обработке ввода. Попробуйте еще раз \uD83D\uDD04";

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
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        Long userId = user.getId();
        log.info("Call command send_message_to_all_users by userId={}, username={}", userId, user.getUserName());
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        if (subscriberService.checkIfAdmin(userId)) {
            MessageDto dto = messageSession.createSession(chatId);
            dto.setStep(1);
            messageSession.save(chatId, dto);
            commandStateStore.setCurrentCommand(userId, "send_message_to_all_users");
            answer.setText("📩 Напишите, что вы хотите отправить подисчикам:");
        }
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /send_message_to_all_users command", e);
        }
    }

    private void handleTextInput(Message message, MessageDto dto, SendMessage answer) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        String text = message.getText();

        switch (dto.getStep()) {
            case 2 -> {
                dto.setMessage(text);
                answer.setText("\uD83D\uDDBC Отправьте фото или картинку (или - если картинки/фото нет):");
                dto.setStep(3);
            }
            case 3 -> answer.setText("\uD83D\uDDBC Пожалуйста, отправьте фото (или - если картинки/фото нет):");
            default -> {
                answer.setText(unknownStep);
                messageSession.clear(chatId);
                commandStateStore.clearCurrentCommand(userId);
            }
        }
    }

    public void processTextInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        if (!"send_message_to_all_users".equals(commandStateStore.getCurrentCommand(userId))) {
            return;
        }

        MessageDto dto = messageSession.getIfExists(chatId);
        if (dto == null) {
            sendErrorMessage(absSender, chatId, sessionExpired);
            commandStateStore.clearCurrentCommand(userId);
            return;
        }
        try {
            handleTextInput(message, dto, answer);
            messageSession.save(chatId, dto);
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error processing text input", e);
            sendErrorMessage(absSender, chatId, errorToProcessInput);
        }
    }

    public void processPhotoInput(AbsSender absSender, Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        if (!"send_message_to_all_users".equals(commandStateStore.getCurrentCommand(userId))) {
            return;
        }

        MessageDto dto = messageSession.getIfExists(chatId);
        if (dto == null || dto.getStep() != 3) {
            sendErrorMessage(absSender, chatId, sessionExpired);
            commandStateStore.clearCurrentCommand(userId);
            return;
        }

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
                eventPublisher.publishEvent(new EventSendMessageToAllUsers(dto.getMessage(), dto.getPhoto(), subscriberService.findAll()));
                answer.setText("Сообщение отправлено всем пользователям ✅");
                messageSession.clear(chatId);
                commandStateStore.clearCurrentCommand(userId);
            } else if (!message.hasPhoto()) {
                log.info("У сообщения для всех пользователей фото/картинки нет");
                eventPublisher.publishEvent(new EventSendMessageToAllUsers(dto.getMessage(), null, subscriberService.findAll()));
                answer.setText("Сообщение отправлено всем пользователям ✅");
                messageSession.clear(chatId);
                commandStateStore.clearCurrentCommand(userId);
            }
            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error processing photo", e);
            sendErrorMessage(absSender, chatId, "Ошибка при создании места ❌");
        }
    }

    private byte[] downloadPhoto(AbsSender absSender, String fileId) throws Exception {
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = absSender.execute(getFileMethod);
        String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + file.getFilePath();
        log.info("Downloading photo from: {}", fileUrl);
        try (InputStream inputStream = new URL(fileUrl).openStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            byte[] result = outputStream.toByteArray();
            log.info("Successfully downloaded photo: {} bytes", result.length);
            return result;
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