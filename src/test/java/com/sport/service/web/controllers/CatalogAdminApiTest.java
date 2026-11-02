package com.sport.service.web.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sport.service.configurations.SecurityConfiguration;
import com.sport.service.exceptions.GlobalExceptionHandler;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.services.CoachService;
import com.sport.service.services.PlaceService;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@SpringJUnitConfig({SecurityConfiguration.class, CatalogAdminApiTest.Config.class})
@WebAppConfiguration
@TestPropertySource(properties = "jwt.secret=catalog-admin-tests-secret-at-least-32-bytes")
class CatalogAdminApiTest {
    private static final String PLACE =
            """
            {"name":"Зал","district":"SOVETSKYY","subdistrict":null,"placeType":"GYM",
             "outdoor":false,"address":"ул. Спортивная, 1","coordinates":"51.65, 39.15"}
            """;
    private static final String COACH =
            """
            {"subscriberUsername":"@trainer","name":"Тренер","sportTypes":["Футбол"],
             "description":"Занятия","age":30,"sex":"Мужчина","yearsOfExperience":5,
             "education":"Спортивное","phoneNumber":"+79991234567","monthsForSubscriptionToBeCoach":3,
             "showInWeb":true,"workPlacesNames":[]}
            """;
    @Autowired WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    Filter security;

    @Autowired PlaceService places;
    @Autowired CoachService coaches;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        reset(places, coaches);
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
    }

    @Test
    void onlyAdminCanReadFormsAndMutateCatalogs() throws Exception {
        for (String role : new String[] {"SUBSCRIBER", ""}) {
            int expected = role.isEmpty() ? 401 : 403;
            for (String catalog : new String[] {"places", "coaches"}) {
                mvc.perform(get("/api/" + catalog + "/1").header("Authorization", token(role)))
                        .andExpect(status().is(expected));
                mvc.perform(delete("/api/" + catalog + "/1").header("Authorization", token(role)))
                        .andExpect(status().is(expected));
                for (String method : new String[] {"POST", "PUT"}) {
                    String url = "/api/" + catalog + (method.equals("PUT") ? "/1" : "");
                    if (catalog.equals("places")) {
                        mvc.perform(
                                        request(
                                                        org.springframework.http.HttpMethod.valueOf(
                                                                method),
                                                        url)
                                                .header("Authorization", token(role))
                                                .contentType("application/json")
                                                .content(PLACE))
                                .andExpect(status().is(expected));
                    } else {
                        mvc.perform(
                                        multipart(url)
                                                .file(data(COACH))
                                                .with(
                                                        request -> {
                                                            request.setMethod(method);
                                                            return request;
                                                        })
                                                .header("Authorization", token(role)))
                                .andExpect(status().is(expected));
                    }
                }
            }
        }
        verifyNoInteractions(places, coaches);
    }

    @Test
    void permissionsAreServerDerived() throws Exception {
        for (String catalog : new String[] {"places", "coaches"}) {
            for (String role : new String[] {"ADMIN", "SUBSCRIBER"}) {
                mvc.perform(
                                get("/api/" + catalog + "/permissions")
                                        .header("Authorization", token(role)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.canManage").value(role.equals("ADMIN")));
            }
        }
    }

    @Test
    void adminCanCreateReadUpdateAndDeletePlace() throws Exception {
        when(places.create(any())).thenReturn(15L);
        mvc.perform(
                        post("/api/places")
                                .header("Authorization", token("ADMIN"))
                                .contentType("application/json")
                                .content(PLACE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15));
        mvc.perform(get("/api/places/15").header("Authorization", token("ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(
                        put("/api/places/15")
                                .header("Authorization", token("ADMIN"))
                                .contentType("application/json")
                                .content(PLACE))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/places/15").header("Authorization", token("ADMIN")))
                .andExpect(status().isNoContent());
        verify(places)
                .update(
                        eq(15L),
                        argThat(
                                place ->
                                        Boolean.FALSE.equals(place.getOutdoor())
                                                && place.getSubdistrict() == null));
        verify(places).deleteById(15L);
        verify(places).getForEditing(15L);
    }

    @Test
    void coachPhotoIsOptionalAndAdminCanPerformAllOperations() throws Exception {
        when(coaches.createCoach(any(), isNull())).thenReturn(22L);
        mvc.perform(
                        multipart("/api/coaches")
                                .file(data(COACH))
                                .header("Authorization", token("ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(22));
        mvc.perform(get("/api/coaches/22").header("Authorization", token("ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(
                        multipart("/api/coaches/22")
                                .file(data(COACH))
                                .with(
                                        request -> {
                                            request.setMethod("PUT");
                                            return request;
                                        })
                                .header("Authorization", token("ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/coaches/22").header("Authorization", token("ADMIN")))
                .andExpect(status().isOk());
        verify(coaches)
                .updateCoachById(
                        eq(22L),
                        argThat(coach -> coach.getSubscriberUsername().equals("@trainer")),
                        isNull());
        verify(coaches).deleteCoachById(22L);
        verify(coaches).getForEditing(22L);
    }

    @Test
    void invalidValuesNeverReachServices() throws Exception {
        for (String body :
                new String[] {
                    "{}",
                    PLACE.replace("SOVETSKYY", "ALL_DISTRICTS"),
                    PLACE.replace("51.65, 39.15", "NaN, 300"),
                    PLACE.replace("\"coordinates\"", "\"webSite\"")
                }) {
            mvc.perform(
                            post("/api/places")
                                    .header("Authorization", token("ADMIN"))
                                    .contentType("application/json")
                                    .content(body))
                    .andExpect(status().isBadRequest());
        }
        for (String body :
                new String[] {
                    "{}",
                    COACH.replace("Футбол", "Invalid"),
                    COACH.replace("Мужчина", "Invalid"),
                    COACH.replace("\"age\":30", "\"age\":null"),
                    COACH.replace("@trainer", ""),
                    COACH.replace("\"yearsOfExperience\":5", "\"yearsOfExperience\":-1")
                }) {
            mvc.perform(
                            multipart("/api/coaches")
                                    .file(data(body))
                                    .header("Authorization", token("ADMIN")))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(places, coaches);
    }

    @Test
    void missingRecordsReturn404() throws Exception {
        doThrow(new NotFoundException("missing")).when(places).deleteById(9L);
        doThrow(new NotFoundException("missing")).when(coaches).getForEditing(9L);
        mvc.perform(delete("/api/places/9").header("Authorization", token("ADMIN")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/coaches/9").header("Authorization", token("ADMIN")))
                .andExpect(status().isNotFound());
    }

    private MockMultipartFile data(String json) {
        return new MockMultipartFile(
                "data", "data.json", "application/json", json.getBytes(StandardCharsets.UTF_8));
    }

    private String token(String role) {
        if (role.isEmpty()) return "";
        return "Bearer "
                + JWT.create()
                        .withSubject("10")
                        .withClaim("role", role)
                        .withExpiresAt(Instant.now().plusSeconds(60))
                        .sign(Algorithm.HMAC256("catalog-admin-tests-secret-at-least-32-bytes"));
    }

    @Configuration
    @EnableWebMvc
    @Import({PlaceController.class, CoachController.class, GlobalExceptionHandler.class})
    static class Config {
        @Bean
        PlaceService places() {
            return mock(PlaceService.class);
        }

        @Bean
        CoachService coaches() {
            return mock(CoachService.class);
        }
    }
}
