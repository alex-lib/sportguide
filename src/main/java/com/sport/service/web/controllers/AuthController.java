package com.sport.service.web.controllers;

import com.sport.service.services.TelegramAuthService;
import com.sport.service.web.models.auth.JwtResponse;
import com.sport.service.web.models.auth.TelegramAuthRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final TelegramAuthService telegramAuthServiceImpl;

    @PostMapping("/telegram")
    public JwtResponse authenticateViaTelegram(@RequestBody TelegramAuthRequest request) {
        log.info("Auth request received");
        return telegramAuthServiceImpl.authenticate(request.getInitData());
    }
}