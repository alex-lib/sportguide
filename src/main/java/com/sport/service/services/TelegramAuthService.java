package com.sport.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sport.service.dto.SubscriberDto;
import com.sport.service.entities.Subscriber;
import com.sport.service.web.models.auth.JwtResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.objects.User;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthService {
    private final SubscriberService subscriberService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${telegram.bot.token}")
    private String botToken;

    private byte[] secretKey;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing TelegramAuthService...");
            log.info("Bot token configured: {}", botToken != null);

            if (botToken == null || botToken.isBlank()) {
                throw new IllegalStateException("Telegram bot token is not configured");
            }

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            this.secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));

            log.info("Secret key initialized using SHA-256(botToken)");
            log.info("Secret key length: {} bytes", secretKey.length); // всегда 32

        } catch (Exception e) {
            log.error("Failed to initialize TelegramAuthService", e);
            throw new IllegalStateException(e);
        }
    }


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
//        if (data.containsKey("signature")) {
//            log.info("Validating using signature (new Telegram WebApp)");
//            log.info("Data contains: {}", data.keySet());
//            log.info("Signature value: {}", data.get("signature"));
//            boolean result = validateSignature(data);
//            log.info("Signature validation result: {}", result);
//            return result;
//        }

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

    private boolean validateSignature(Map<String, String> data) {
        try {
            String signature = data.get("signature");
            if (signature == null) {
                log.error("Signature is null");
                return false;
            }

            log.info("=== SIGNATURE VALIDATION ===");
            log.info("Signature received (first 20 chars): {}...",
                    signature.substring(0, Math.min(20, signature.length())));
            log.info("Signature length: {}", signature.length());

            if (secretKey == null || secretKey.length == 0) {
                log.error("Secret key is not initialized!");
                return false;
            }

            log.info("Secret key length: {} bytes", secretKey.length);
            log.info("Secret key preview: {}...",
                    new String(secretKey, 0, Math.min(10, secretKey.length)));

            Map<String, String> filtered = data.entrySet().stream()
                    .filter(e -> !e.getKey().equals("signature"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            log.info("Fields for validation: {}", filtered.keySet());

            String dataCheckString = filtered.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> {
                        return e.getKey() + "=" + e.getValue();
                    })
                    .collect(Collectors.joining("\n"));

            log.info("Data check string ({} chars)", dataCheckString.length());
            log.debug("Data check string:\n{}", dataCheckString);

            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] calculated = mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            String base64Standard = Base64.getEncoder().encodeToString(calculated);
            String calculatedBase64Url = base64Standard
                    .replace('+', '-')
                    .replace('/', '_')
                    .replace("=", "");

            log.info("=== CALCULATION RESULTS ===");
            log.info("Calculated Base64 URL-safe: {}", calculatedBase64Url);
            log.info("Calculated length: {}", calculatedBase64Url.length());
            log.info("Received signature: {}", signature);

            String calculatedHex = bytesToHex(calculated);
            log.info("Calculated Hex: {}...", calculatedHex.substring(0, Math.min(32, calculatedHex.length())));

            boolean match = calculatedBase64Url.equals(signature);
            log.info("Signature match: {}", match);

            if (!match) {
                log.warn("=== MISMATCH DIAGNOSTICS ===");
                log.warn("Expected length: {}, Actual length: {}",
                        calculatedBase64Url.length(), signature.length());

                int compareLength = Math.min(20, Math.min(calculatedBase64Url.length(), signature.length()));
                for (int i = 0; i < compareLength; i++) {
                    if (calculatedBase64Url.charAt(i) != signature.charAt(i)) {
                        log.warn("First mismatch at position {}: expected '{}', got '{}'",
                                i, calculatedBase64Url.charAt(i), signature.charAt(i));
                        break;
                    }
                }

                boolean standardBase64Match = base64Standard.equals(signature);
                log.info("Standard Base64 match: {}", standardBase64Match);

                boolean hexMatch = calculatedHex.equals(signature);
                log.info("Hex match: {}", hexMatch);

                return standardBase64Match || hexMatch;
            }

            return true;

        } catch (Exception e) {
            log.error("Signature validation error: {}", e.getMessage(), e);
            return false;
        }
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

            log.debug("Data check string:\n{}", dataCheckString);


            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
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

//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class TelegramAuthService {
//    private final SubscriberService subscriberService;
//    private final JwtService jwtService;
//    private final ObjectMapper objectMapper;
//
//    @Value("${telegram.bot.token}")
//    private String botToken;
//
//    private byte[] secretKey;
//
//    @PostConstruct
//    public void init() throws Exception {
//        this.secretKey = MessageDigest
//                .getInstance("SHA-256")
//                .digest(botToken.getBytes());
//    }
//
//    //https://github.com/riobits/Telegram-Web-API-Cheatsheet?utm_source=chatgpt.com
//    public JwtResponse authenticate(String initData) {
//        log.info("Raw initData received: {}", initData);
//        log.info("InitData length: {}", initData.length());
//        Map<String, String> data = parseInitData(initData);
//        log.info("Parsed data keys: {}", data.keySet());
//        log.info("Parsed data: {}", data);
////        if ("DEV_MODE".equals(initData)) {
////            Subscriber devUser = subscriberService.findOrCreateDevUser();
////            return new JwtResponse(jwtService.generateToken(devUser));
////        }
//
//
//        if (!validateTelegramHash(data)) {
//            log.error("InitData is null or empty");
//            throw new RuntimeException("Invalid Telegram auth data");
//        }
//
//        if (!data.containsKey("hash")) {
//            log.error("No hash found in initData");
//            throw new RuntimeException("Invalid Telegram auth data: missing hash");
//        }
//
//        if (!data.containsKey("user")) {
//            log.error("No user found in initData");
//            throw new RuntimeException("Invalid Telegram auth data: missing user");
//        }
//
//        log.info("Received hash: {}", data.get("hash"));
//        log.info("User JSON: {}", data.get("user"));
//
//        String userJson = data.get("user");
//        if (userJson == null) throw new RuntimeException("Invalid Telegram auth data");
//
//        SubscriberDto tgUser = null;
//        try {
//            tgUser = objectMapper.readValue(userJson, SubscriberDto.class);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//        Subscriber subscriber = subscriberService.findById(tgUser.getId());
//
//        if (subscriber != null) {
//            subscriber.setUsername(tgUser.getUsername());
//            subscriber.setFirstName(tgUser.getFirstName());
//            subscriber.setLastName(tgUser.getLastName());
//            subscriberService.updateSubscriber(subscriber, tgUser.getId());
//        } else {
//            User user = new User();
//            user.setId(tgUser.getId());
//            user.setUserName(tgUser.getUsername());
//            user.setFirstName(tgUser.getFirstName());
//            user.setLastName(tgUser.getLastName());
//
//            subscriberService.addSubscriber(user);
//        }
//
//        return new JwtResponse(jwtService.generateToken(subscriberService.findById(tgUser.getId())));
//    }
//
//    //example:
//    //query_id=AAGXJt8AAAAAAafJt3xYdPq3 - id сессии webapp (может отсутствовать)
//    //&user=%7B%22id%22%3A123456789%2C%22username%22%3A%22alex_sport%22%2C%22first_name%22%3A%22Алекс%22%2C%22last_name%22%3A%22Иванов%22%2C%22language_code%22%3A%22ru%22%7D - json, но encoded
//    //&auth_date=123 - timestamp, когда telegram сгенерировал данные
//    //&hash=9e4f2b4c6f0c3d8e7b3b1e6c1a2d4f8a9c7e4b2ff3c9d1a6b4f2c8c9d1e0f2a - подпись, для проверки подлинности данных
//    private Map<String, String> parseInitData(String initData) {
//        Map<String, String> result = new HashMap<>();
//        log.debug("Parsing initData: {}", initData);
////        if (!initData.contains("=")) {
////            return result;
////        }
//
//        for (String pair : initData.split("&")) {
//
//            if (!pair.contains("=")) {
//                log.warn("Skipping malformed pair: {}", pair);
//                continue;
//            }
//
//            String[] keyAndValue = pair.split("=", 2);
//            String key = keyAndValue[0];
//            String value = URLDecoder.decode(keyAndValue[1], StandardCharsets.UTF_8);
//            log.debug("Parsed: {} = {}", key, value);
//            result.put(key, value);
//        }
//
//        return result;
//    }
//
//    private boolean validateTelegramHash(Map<String, String> data) {
//        try {
//            String signature = data.get("signature");
//            String receivedHash = data.get("hash");
//
//            if (signature != null) {
//                return validateSignature(data);
//            } else if (receivedHash != null) {
//                return validateOldHash(data);
//            }
//
//            return false;
//
//        } catch (Exception e) {
//            log.error("Hash validation error: {}", e.getMessage(), e);
//            return false;
//        }
//    }
//
//    private boolean validateSignature(Map<String, String> data) {
//        try {
//            String signature = data.get("signature");
//            if (signature == null) return false;
//
//            Map<String, String> filtered = data.entrySet().stream()
//                    .filter(e -> !e.getKey().equals("signature"))
//                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//            String dataCheckString = filtered.entrySet().stream()
//                    .sorted(Map.Entry.comparingByKey())
//                    .map(e -> e.getKey() + "=" + e.getValue())
//                    .collect(Collectors.joining("\n"));
//
//            Mac mac = Mac.getInstance("HmacSHA256");
//            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
//            byte[] calculated = mac.doFinal(dataCheckString.getBytes());
//
//            String calculatedSignature = IntStream.range(0, calculated.length)
//                    .mapToObj(i -> String.format("%02x", calculated[i] & 0xff))
//                    .collect(Collectors.joining());
//
//            log.info("Calculated signature: {}", calculatedSignature);
//            log.info("Received signature:  {}", signature);
//
//            return calculatedSignature.equals(signature);
//
//        } catch (Exception e) {
//            log.error("Signature validation error: {}", e.getMessage(), e);
//            return false;
//        }
//    }
//
//    private boolean validateOldHash(Map<String, String> data) {
//        String receivedHash = data.get("hash");
//        if (receivedHash == null) return false;
//
//        Map<String, String> filtered = data.entrySet().stream()
//                .filter(e -> !e.getKey().equals("hash"))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//        String dataCheckString = filtered.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .map(e -> e.getKey() + "=" + e.getValue())
//                .collect(Collectors.joining("\n"));
//
//        try {
//            Mac mac = Mac.getInstance("HmacSHA256");
//            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
//            byte[] calculated = mac.doFinal(dataCheckString.getBytes());
//
//            String calculatedHash = IntStream.range(0, calculated.length)
//                    .mapToObj(i -> String.format("%02x", calculated[i] & 0xff))
//                    .collect(Collectors.joining());
//
//            return calculatedHash.equals(receivedHash);
//        } catch (Exception e) {
//            return false;
//        }
//    }
//    private boolean validateTelegramHash(Map<String, String> data) {
//        String receivedHash = data.get("hash");
//        log.info("Validating hash: {}", receivedHash);
//        if (receivedHash == null) return false;
//
//        Map<String, String> filtered = data.entrySet().stream()
//                .filter(e -> !e.getKey().equals("hash"))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//        String dataCheckString = filtered.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .map(e -> e.getKey() + "=" + e.getValue())
//                .collect(Collectors.joining("\n"));
//        //example:
//        //auth_date=123
//        //user={"id":123}
//        //query_id=AAGXJt8AAAAAAafJt3xYdPq3
//
//        log.info("Data check string:\n{}", dataCheckString);
//        log.info("Data check string bytes: {}", dataCheckString.getBytes());
//
//        try {
//            Mac mac = Mac.getInstance("HmacSHA256");
//            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
//            byte[] calculated = mac.doFinal(dataCheckString.getBytes());
//
//            String calculatedHash = IntStream.range(0, calculated.length)
//                    .mapToObj(i -> String.format("%02x", calculated[i] & 0xff))
//                    .collect(Collectors.joining());
//
////          or  String calculatedHash = HexFormat.of().formatHex(calculated);
//            log.info("Calculated hash: {}", calculatedHash);
//            log.info("Received hash:  {}", receivedHash);
//            log.info("Match: {}", calculatedHash.equals(receivedHash));
//            return calculatedHash.equals(receivedHash);
//
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}