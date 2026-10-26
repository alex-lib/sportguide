package com.sport.service.configurations;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig({SecurityConfiguration.class, SecurityConfigurationTest.WebConfig.class})
@WebAppConfiguration
@TestPropertySource(properties = "jwt.secret=" + SecurityConfigurationTest.SECRET)
class SecurityConfigurationTest {
    static final String SECRET = "test-only-secret-with-at-least-32-bytes";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter securityFilter;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilter).build();
    }

    @Test
    void publicTelegramLoginAndPhotosRemainAccessible() throws Exception {
        mvc.perform(post("/api/auth/telegram")).andExpect(status().isOk());
        mvc.perform(get("/api/places/photo")).andExpect(status().isOk());
    }

    @Test
    void protectedApiRejectsMissingOrInvalidTokens() throws Exception {
        mvc.perform(get("/api/test")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/test").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUBSCRIBER", "ADMIN"})
    void existingTelegramTokensAuthenticate(String role) throws Exception {
        mvc.perform(get("/api/test").header("Authorization", "Bearer " + token(role)))
                .andExpect(status().isOk());
    }

    @Test
    void adminApiStillRequiresAdminRole() throws Exception {
        mvc.perform(get("/api/admin-test").header("Authorization", "Bearer " + token("SUBSCRIBER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin-test").header("Authorization", "Bearer " + token("ADMIN")))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin/test", "/actuator/health", "/oauth2/authorization/legacy", "/login/test"})
    void retiredEndpointsRemainClosed(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isForbidden());
        mvc.perform(get(path).header("Authorization", "Bearer " + token("ADMIN")))
                .andExpect(status().isForbidden());
    }

    private String token(String role) {
        return JWT.create()
                .withSubject("123")
                .withClaim("role", role)
                .withExpiresAt(Instant.now().plusSeconds(60))
                .sign(Algorithm.HMAC256(SECRET));
    }

    @Configuration
    @EnableWebMvc
    @Import(TestController.class)
    static class WebConfig {
    }

    @RestController
    static class TestController {
        @GetMapping({"/api/test", "/api/places/photo", "/admin/test", "/actuator/health", "/login/test",
                "/oauth2/authorization/legacy"})
        public String get() {
            return "ok";
        }

        @PostMapping("/api/auth/telegram")
        public String login() {
            return "ok";
        }

        @GetMapping("/api/admin-test")
        @PreAuthorize("hasRole('ADMIN')")
        public String admin() {
            return "ok";
        }
    }
}
