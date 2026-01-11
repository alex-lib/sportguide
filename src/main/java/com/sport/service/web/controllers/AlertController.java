//package com.sport.service.web.controllers;
//
//import com.sport.service.bot.TelegramMessageSender;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/alerts")
//public class AlertController {
//
//    private final String mainAdminId;
//    private final TelegramMessageSender sender;
//
//    public AlertController(
//            @Value("${telegram.mainAdminId}") String mainAdminId,
//            TelegramMessageSender sender
//    ) {
//        this.mainAdminId = mainAdminId;
//        this.sender = sender;
//    }
//
//    @PostMapping("/alerts")
//    public void receiveAlert(@RequestBody String rawJson) {
//        sender.sendMessageWithoutPhoto(Long.parseLong(mainAdminId), rawJson);
//    }
//}