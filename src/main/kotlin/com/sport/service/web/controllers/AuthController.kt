//package com.sport.service.web.controllers
//
//import com.sport.service.services.impl.TelegramAuthServiceImpl
//import com.sport.service.web.models.auth.JwtResponse
//import com.sport.service.web.models.auth.TelegramAuthRequest
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestBody
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/auth")
//class AuthController(
//    private var telegramAuthServiceImpl: TelegramAuthServiceImpl,
//) {
//
//    @PostMapping("/telegram")
//    fun authenticateViaTelegram(
//        @RequestBody request: TelegramAuthRequest
//    ): JwtResponse {
//        return telegramAuthServiceImpl.authenticate(request.initData)
//    }
//}