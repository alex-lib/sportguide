package com.sport.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sport.service.dto.SubscriberDto;
import com.sport.service.entities.Subscriber;
import com.sport.service.web.models.auth.JwtResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.objects.User;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
@RequiredArgsConstructor
public class TelegramAuthService {
    private final SubscriberService subscriberService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${telegram.bot.token}")
    private String botToken;

    private byte[] secretKey;

    @PostConstruct
    public void init() throws Exception {
        this.secretKey = MessageDigest
                .getInstance("SHA-256")
                .digest(botToken.getBytes());
    }

    //https://github.com/riobits/Telegram-Web-API-Cheatsheet?utm_source=chatgpt.com
    public JwtResponse authenticate(String initData) {
        Map<String, String> data = parseInitData(initData);

//        if ("DEV_MODE".equals(initData)) {
//            Subscriber devUser = subscriberService.findOrCreateDevUser();
//            return new JwtResponse(jwtService.generateToken(devUser));
//        }

        if (!validateTelegramHash(data)) {
            throw new RuntimeException("Invalid Telegram auth data");
        }

        String userJson = data.get("user");
        if (userJson == null) throw new RuntimeException("Invalid Telegram auth data");

        SubscriberDto tgUser = null;
        try {
            tgUser = objectMapper.readValue(userJson, SubscriberDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Subscriber subscriber = subscriberService.findById(tgUser.getId());

        if (subscriber != null) {
            subscriber.setUsername(tgUser.getUsername());
            subscriber.setFirstName(tgUser.getFirstName());
            subscriber.setLastName(tgUser.getLastName());
            subscriberService.updateSubscriber(subscriber, tgUser.getId());
        } else {
            User user = new User();
            user.setId(tgUser.getId());
            user.setUserName(tgUser.getUsername());
            user.setFirstName(tgUser.getFirstName());
            user.setLastName(tgUser.getLastName());

            subscriberService.addSubscriber(user);
        }

        return new JwtResponse(jwtService.generateToken(subscriberService.findById(tgUser.getId())));
    }

    //example:
    //query_id=AAGXJt8AAAAAAafJt3xYdPq3 - id сессии webapp (может отсутствовать)
    //&user=%7B%22id%22%3A123456789%2C%22username%22%3A%22alex_sport%22%2C%22first_name%22%3A%22Алекс%22%2C%22last_name%22%3A%22Иванов%22%2C%22language_code%22%3A%22ru%22%7D - json, но encoded
    //&auth_date=123 - timestamp, когда telegram сгенерировал данные
    //&hash=9e4f2b4c6f0c3d8e7b3b1e6c1a2d4f8a9c7e4b2ff3c9d1a6b4f2c8c9d1e0f2a - подпись, для проверки подлинности данных
    private Map<String, String> parseInitData(String initData) {
        Map<String, String> result = new HashMap<>();

//        if (!initData.contains("=")) {
//            return result;
//        }

        for (String pair : initData.split("&")) {
            if (!pair.contains("=")) continue;

            String[] keyAndValue = pair.split("=", 2);
            String key = keyAndValue[0];
            String value = URLDecoder.decode(keyAndValue[1], StandardCharsets.UTF_8);
            result.put(key, value);
        }

        return result;
    }

    private boolean validateTelegramHash(Map<String, String> data) {
        String receivedHash = data.get("hash");
        if (receivedHash == null) return false;

        Map<String, String> filtered = data.entrySet().stream()
                .filter(e -> !e.getKey().equals("hash"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String dataCheckString = filtered.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));
        //example:
        //auth_date=123
        //user={"id":123}
        //query_id=AAGXJt8AAAAAAafJt3xYdPq3

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] calculated = mac.doFinal(dataCheckString.getBytes());

            String calculatedHash = IntStream.range(0, calculated.length)
                    .mapToObj(i -> String.format("%02x", calculated[i] & 0xff))
                    .collect(Collectors.joining());

//          or  String calculatedHash = HexFormat.of().formatHex(calculated);

            return calculatedHash.equals(receivedHash);

        } catch (Exception e) {
            return false;
        }
    }
}