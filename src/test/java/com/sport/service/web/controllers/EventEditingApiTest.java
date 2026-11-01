package com.sport.service.web.controllers;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sport.service.configurations.SecurityConfiguration;
import com.sport.service.exceptions.GlobalExceptionHandler;
import com.sport.service.services.EventService;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@SpringJUnitConfig({SecurityConfiguration.class, EventEditingApiTest.Config.class})
@WebAppConfiguration
@TestPropertySource(properties = "jwt.secret=test-event-editing-secret-at-least-32-bytes")
class EventEditingApiTest {
    private static final String BODY =
            """
            {"version":0,"name":"Матч","description":"Описание","date":"2026-11-07","time":"14:00",
             "district":"SOVETSKYY","address":"ул. Писателя Маршака, 1г","placeName":"Стадион Факел",
             "link":"https://fakelfc.ru/matches/2997","coordinates":"51.65, 39.15"}
            """;
    @Autowired WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    Filter security;

    @Autowired EventService service;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        reset(service);
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
    }

    @Test
    void anonymousCannotReadEditDataOrWrite() throws Exception {
        mvc.perform(get("/api/events/1")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/events/1").contentType("application/json").content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void subscriberCannotOpenOrSaveEvenWithDirectApiRequest() throws Exception {
        mvc.perform(get("/api/events/permissions").header("Authorization", token("SUBSCRIBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canEdit").value(false));
        mvc.perform(get("/api/events/1").header("Authorization", token("SUBSCRIBER")))
                .andExpect(status().isForbidden());
        mvc.perform(
                        put("/api/events/1")
                                .header("Authorization", token("SUBSCRIBER"))
                                .contentType("application/json")
                                .content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void adminCanOpenAndSaveAllFields() throws Exception {
        mvc.perform(get("/api/events/permissions").header("Authorization", token("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canEdit").value(true));
        mvc.perform(get("/api/events/1").header("Authorization", token("ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(
                        put("/api/events/1")
                                .header("Authorization", token("ADMIN"))
                                .contentType("application/json")
                                .content(BODY))
                .andExpect(status().isOk());
        verify(service).getForEditing(1L);
        verify(service)
                .updateEvent(
                        eq(1L),
                        argThat(
                                request ->
                                        request.time().getHour() == 14
                                                && request.coordinates().equals("51.65, 39.15")));
    }

    @Test
    void rejectsIncompleteOrUnsafeValues() throws Exception {
        for (String body :
                new String[] {
                    "{}",
                    BODY.replace("SOVETSKYY", "ALL_DISTRICTS"),
                    BODY.replace("https://fakelfc.ru/matches/2997", "javascript:alert(1)"),
                    BODY.replace("51.65, 39.15", "NaN, 300"),
                    BODY.replace("14:00", "29:00")
                }) {
            mvc.perform(
                            put("/api/events/1")
                                    .header("Authorization", token("ADMIN"))
                                    .contentType("application/json")
                                    .content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test
    void adminCanSaveEventWithTimeStillUnknown() throws Exception {
        mvc.perform(
                        put("/api/events/1")
                                .header("Authorization", token("ADMIN"))
                                .contentType("application/json")
                                .content(BODY.replace("\"14:00\"", "null")))
                .andExpect(status().isOk());
        verify(service).updateEvent(eq(1L), argThat(request -> request.time() == null));
    }

    private String token(String role) {
        return "Bearer "
                + JWT.create()
                        .withSubject("10")
                        .withClaim("role", role)
                        .withExpiresAt(Instant.now().plusSeconds(60))
                        .sign(Algorithm.HMAC256("test-event-editing-secret-at-least-32-bytes"));
    }

    @Configuration
    @EnableWebMvc
    @Import({EventController.class, GlobalExceptionHandler.class})
    static class Config {
        @Bean
        EventService eventService() {
            return mock(EventService.class);
        }
    }
}
