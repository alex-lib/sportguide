package com.sport.service.web.models.ai;

public class AskResponse {
    private String answer;

    public AskResponse() {
    }

    public AskResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}