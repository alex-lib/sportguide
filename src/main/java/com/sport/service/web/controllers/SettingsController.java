package com.sport.service.web.controllers;

import com.sport.service.services.NotificationPreferenceService;
import com.sport.service.web.models.settings.NotificationPreferences;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings/notifications")
@RequiredArgsConstructor
public class SettingsController {
    private final NotificationPreferenceService preferences;

    @GetMapping
    public NotificationPreferences get(Authentication authentication) {
        return preferences.get(Long.parseLong(authentication.getName()));
    }

    @PutMapping
    public NotificationPreferences update(Authentication authentication, @Valid @RequestBody NotificationPreferences request) {
        return preferences.update(Long.parseLong(authentication.getName()), request);
    }
}
