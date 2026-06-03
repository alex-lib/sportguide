package com.sport.service.web.controllers;

import com.sport.service.services.ai.AiAssistantService;
import com.sport.service.web.models.ai.AskRequest;
import com.sport.service.web.models.ai.AskResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/ai", produces = MediaType.APPLICATION_JSON_VALUE)
public class AiController {

    private final AiAssistantService aiAssistantService;

    public AiController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping(path = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        String answer = aiAssistantService.generateTrainingProgram(request.getQuestion());
        return new AskResponse(answer);
    }
}