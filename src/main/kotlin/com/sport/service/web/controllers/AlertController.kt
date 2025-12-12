//package com.sport.service.web.controllers
//
//import com.sport.service.bot.TelegramMessageSender
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestBody
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/alerts")
//class AlertController(
//    @Value("\${telegram.mainAdminId}") private val mainAdminId: String,
//    private val sender: TelegramMessageSender
//) {
//
//    @PostMapping("/alerts")
//    fun receiveAlert(@RequestBody rawJson: String) {
//        sender.sendMessageWithoutPhoto(mainAdminId.toLong(), rawJson)
//    }
//}