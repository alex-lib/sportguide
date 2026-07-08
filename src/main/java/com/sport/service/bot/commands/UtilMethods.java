package com.sport.service.bot.commands;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
public class UtilMethods {

    public static byte[] downloadPhoto(AbsSender absSender, String fileId, String botToken) {
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(fileId);
        File file;
        try {
            file = absSender.execute(getFileMethod);
        } catch (TelegramApiException e) {
            log.error("Error download photo", e);
            throw new RuntimeException("Failed to fetch file metadata: " + e.getMessage(), e);
        }
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
        } catch (IOException e) {
            log.error("Failed to download photo from URL: {}", fileUrl, e);
            throw new RuntimeException("Failed to download photo: " + e.getMessage(), e);
        }
    }
}