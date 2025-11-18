package com.sport.service.web.controllers;

import com.sport.service.bot.TelegramMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {
    @Value("${telegram.mainAdminId}")
    private String mainAdminId;

    private final TelegramMessageSender sender;

    @PostMapping("/alerts")
    public void receiveAlert(@RequestBody String rawJson) {
        sender.sendMessageWithoutPhoto(Long.valueOf(mainAdminId), rawJson);
    }
}