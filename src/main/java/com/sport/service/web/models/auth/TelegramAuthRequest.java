package com.sport.service.web.models.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class TelegramAuthRequest {
    private String initData;
}
