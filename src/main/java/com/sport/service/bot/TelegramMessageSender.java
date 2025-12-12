package com.sport.service.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramMessageSender {
    private final ApplicationContext context;

    private SportGuideBot getBot() {
        return context.getBean(SportGuideBot.class);
    }

    public void sendMessageWithoutPhoto(Long userId, String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(userId)
                .text(message)
                .parseMode("HTML")
                .build();
        try {
            getBot().execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message without photo: {}", message, e);
        }
    }

    public void sendMessageWithPhoto(Long userId, byte[] photo, String message) {
        SendPhoto photoMessage = new SendPhoto();
        try (InputStream photoStream = new ByteArrayInputStream(photo)) {
            photoMessage.setPhoto(new InputFile(photoStream, "place.jpg"));
            photoMessage.setCaption(message);
            photoMessage.setParseMode("HTML");
            photoMessage.setChatId(userId);
        } catch (IOException e) {
            log.error("Failed to process photo {}", message, e);
        }
        try {
            getBot().execute(photoMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message with photo: {}", message, e);
        }
    }
}