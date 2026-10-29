package com.sport.service.web.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sport.service.configurations.SecurityConfiguration;
import com.sport.service.exceptions.GlobalExceptionHandler;
import com.sport.service.services.NotificationPreferenceService;
import com.sport.service.services.JointTrainingService;
import com.sport.service.services.ai.TrainingGenerationService;
import com.sport.service.web.models.settings.NotificationPreferences;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig({SecurityConfiguration.class, UserSettingsAndAiApiTest.WebConfig.class})
@WebAppConfiguration
@TestPropertySource(properties = "jwt.secret=" + UserSettingsAndAiApiTest.SECRET)
class UserSettingsAndAiApiTest {
    static final String SECRET = "test-only-secret-with-at-least-32-bytes";
    private static final String SETTINGS = "/api/settings/notifications";
    private static final String BODY = "{\"weather\":true,\"events\":false,\"places\":true,\"coaches\":false,\"jointTrainings\":false}";
    @Autowired private WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") private Filter security;
    @Autowired private NotificationPreferenceService preferences;
    @Autowired private TrainingGenerationService generation;
    @Autowired private JointTrainingService jointTrainings;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        reset(preferences, generation, jointTrainings);
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
    }

    @Test
    void settingsAndAiRequireAuthentication() throws Exception {
        mvc.perform(get(SETTINGS)).andExpect(status().isUnauthorized());
        mvc.perform(put(SETTINGS).contentType("application/json").content(BODY)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/ai/ask").contentType("application/json").content("{\"question\":\"Бег\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(preferences, generation);
    }

    @Test
    void settingsUseJwtSubjectAndIgnoreUserIdFromClient() throws Exception {
        var settings = new NotificationPreferences(true, false, true, false, false);
        when(preferences.get(10L)).thenReturn(settings);
        when(preferences.update(10L, settings)).thenReturn(settings);
        mvc.perform(get(SETTINGS).param("userId", "999").header("Authorization", token()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.weather").value(true));
        mvc.perform(put(SETTINGS).param("userId", "999").header("Authorization", token())
                        .contentType("application/json").content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.events").value(false));
        verify(preferences).update(10L, settings);
        verify(preferences, never()).update(eq(999L), any());
    }

    @Test
    void incompleteSettingsCannotResetOtherPreferences() throws Exception {
        mvc.perform(put(SETTINGS).header("Authorization", token()).contentType("application/json")
                        .content("{\"weather\":true}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(preferences);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUBSCRIBER", "ADMIN"})
    void aiGenerationIsUnavailableThroughWebApi(String role) throws Exception {
        mvc.perform(post("/api/ai/ask").header("Authorization", token(role)).contentType("application/json")
                        .content("{\"question\":\"Бег\",\"userId\":999}"))
                .andExpect(status().isNotFound());
        verifyNoInteractions(generation);
    }

    private String token() {
        return token("SUBSCRIBER");
    }

    private String token(String role) {
        return "Bearer " + JWT.create().withSubject("10").withClaim("role", role)
                .withExpiresAt(Instant.now().plusSeconds(60)).sign(Algorithm.HMAC256(SECRET));
    }

    @Test
    void jointTrainingWritesUseAuthenticatedTelegramId() throws Exception {
        String body = "{\"title\":\"Бег\",\"sportType\":\"RUNNING\",\"district\":\"Центральный\"}";
        mvc.perform(post("/api/joint-trainings").header("Authorization", token())
                        .contentType("application/json").content(body))
                .andExpect(status().isOk());
        verify(jointTrainings).createJointTraining(any(), eq(10L));
        mvc.perform(put("/api/joint-trainings/42").header("Authorization", token())
                        .contentType("application/json").content(body))
                .andExpect(status().isOk());
        verify(jointTrainings).updateJointTrainingById(any(), eq(42L), eq(10L));
        mvc.perform(delete("/api/joint-trainings/42").header("Authorization", token()))
                .andExpect(status().isOk());
        verify(jointTrainings).deleteJointTrainingById(42L, 10L);
    }

    @Configuration
    @EnableWebMvc
    @Import({SettingsController.class, JointTrainingController.class, GlobalExceptionHandler.class})
    static class WebConfig {
        @Bean NotificationPreferenceService preferences() { return mock(NotificationPreferenceService.class); }
        @Bean TrainingGenerationService generation() { return mock(TrainingGenerationService.class); }
        @Bean JointTrainingService jointTrainings() { return mock(JointTrainingService.class); }
    }
}
