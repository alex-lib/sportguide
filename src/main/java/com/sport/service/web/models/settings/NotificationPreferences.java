package com.sport.service.web.models.settings;

import jakarta.validation.constraints.NotNull;

public record NotificationPreferences(
        @NotNull Boolean weather,
        @NotNull Boolean events,
        @NotNull Boolean places,
        @NotNull Boolean coaches,
        @NotNull Boolean jointTrainings) {
}
