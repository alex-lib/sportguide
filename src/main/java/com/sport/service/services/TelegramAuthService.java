package com.sport.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sport.service.dto.SubscriberDto;
import com.sport.service.entities.Subscriber;
import com.sport.service.security.JwtService;
import com.sport.service.web.models.auth.JwtResponse;
//import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.objects.User;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;

//import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthService {
    private final SubscriberService subscriberService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${telegram.bot.token}")
    private String botToken;

//    private byte[] secretKey;

//    @PostConstruct
//    public void init() {
//        try {
//            log.info("Initializing TelegramAuthService...");
//            log.info("Bot token configured: {}", botToken != null);
//
//            if (botToken == null || botToken.isBlank()) {
//                throw new IllegalStateException("Telegram bot token is not configured");
//            }
//
//            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
//            this.secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));
//
//            log.info("secret key from init: {}", Arrays.toString(secretKey));
//            log.info("Secret key initialized using SHA-256(botToken)");
//
//        } catch (Exception e) {
//            log.error("Failed to initialize TelegramAuthService", e);
//            throw new IllegalStateException(e);
//        }
//    }

    public JwtResponse authenticate(String initData) {
        log.info("Authenticating Telegram user...");

        try {
            Map<String, String> data = parseInitData(initData);
            log.debug("Parsed data keys: {}", data.keySet());

            if (!data.containsKey("user")) {
                log.error("User data not found in initData");
                throw new RuntimeException("Invalid Telegram auth data");
            }

            if (!validateTelegramData(data)) {
                log.error("Telegram data validation failed");
                throw new RuntimeException("Invalid Telegram auth data");
            }

            String userJson = data.get("user");
            SubscriberDto tgUser = objectMapper.readValue(userJson, SubscriberDto.class);
            log.info("User authenticated: {} (@{})",
                    tgUser.getFirstName(), tgUser.getUsername());

            Subscriber subscriber = processUser(tgUser);

            String token = jwtService.generateToken(subscriber);
            return new JwtResponse(token);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse user JSON: {}", e.getMessage());
            throw new RuntimeException("Invalid Telegram auth data");
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid Telegram auth data");
        }
    }

    private boolean validateTelegramData(Map<String, String> data) {

        if (data.containsKey("hash")) {
            log.info("Validating using hash (old Telegram WebApp)");
            log.info("Data contains: {}", data.keySet());
            log.info("Hash value: {}", data.get("hash"));
            boolean result = validateHash(data);
            log.info("Hash validation result: {}", result);
            return result;
        }

        log.error("Neither signature nor hash found in Telegram data");
        log.error("Available keys: {}", data.keySet());

        log.error("Neither signature nor hash found in Telegram data");
        return false;
    }

    private boolean validateHash(Map<String, String> data) {
        try {
            String receivedHash = data.get("hash");
            if (receivedHash == null) {
                log.error("Hash is null");
                return false;
            }

            Map<String, String> filtered = data.entrySet().stream()
                    .filter(e -> !e.getKey().equals("hash"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            String dataCheckString = filtered.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("\n"));

            log.info("Data check string:\n{}", dataCheckString);

            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(botToken.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(keySpec);
            byte[] correctSecretKey = hmacSha256.doFinal(botToken.getBytes(StandardCharsets.UTF_8));

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(correctSecretKey, "HmacSHA256"));
            byte[] calculated = mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            String calculatedHash = bytesToHex(calculated);

            log.info("Calculated hash: {}", calculatedHash);
            log.info("Received hash:   {}", receivedHash);

            return calculatedHash.equals(receivedHash);

        } catch (Exception e) {
            log.error("Hash validation error", e);
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private Map<String, String> parseInitData(String initData) {
        Map<String, String> result = new HashMap<>();

        if (initData == null || initData.trim().isEmpty()) {
            return result;
        }

        try {
            for (String pair : initData.split("&")) {
                if (!pair.contains("=")) {
                    log.warn("Skipping malformed pair: {}", pair);
                    continue;
                }

                String[] keyAndValue = pair.split("=", 2);
                String key = keyAndValue[0];
                String value = URLDecoder.decode(keyAndValue[1], StandardCharsets.UTF_8);

                result.put(key, value);
            }
        } catch (Exception e) {
            log.error("Error parsing initData: {}", e.getMessage());
        }

        return result;
    }


    private Subscriber processUser(SubscriberDto tgUser) {
        Subscriber subscriber = subscriberService.findById(tgUser.getId());

        if (subscriber != null) {
            subscriber.setUsername(tgUser.getUsername());
            subscriber.setFirstName(tgUser.getFirstName());
            subscriber.setLastName(tgUser.getLastName());
            subscriberService.updateSubscriber(subscriber, tgUser.getId());
            log.info("Updated existing user: {}", tgUser.getId());
        } else {
            User user = new User();
            user.setId(tgUser.getId());
            user.setUserName(tgUser.getUsername());
            user.setFirstName(tgUser.getFirstName());
            user.setLastName(tgUser.getLastName());
            subscriberService.addSubscriber(user);
            subscriber = subscriberService.findById(tgUser.getId());
            log.info("Created new user: {}", tgUser.getId());
        }
        return subscriber;
    }
}