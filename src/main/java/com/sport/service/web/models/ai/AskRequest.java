package com.sport.service.web.models.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AskRequest {

    @NotBlank(message = "Question must be provided")
    @Size(max = 1000, message = "Question is too long")
    private String question;
}