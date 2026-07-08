package com.sport.service.web.models.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AskRequest {
    @NotBlank(message = "Question must be provided")
    @Size(max = 1000, message = "Question is too long")
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}