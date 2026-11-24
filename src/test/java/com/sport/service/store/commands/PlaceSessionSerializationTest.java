package com.sport.service.store.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.sport.service.configurations.RedisBotCommandSessionConfiguration;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;

import jakarta.validation.Validation;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.StandardCharsets;

class PlaceSessionSerializationTest {
    @SuppressWarnings("unchecked")
    private final RedisSerializer<Object> serializer =
            (RedisSerializer<Object>)
                    new RedisBotCommandSessionConfiguration()
                            .botCommandSessionRedisTemplate(mock(RedisConnectionFactory.class))
                            .getValueSerializer();

    @Test
    void newSessionCanBeReadAfterFirstMenuIsSent() {
        PlaceDto session = new PlaceDto();
        session.setStep(1);
        byte[] stored = serializer.serialize(session);
        String json = new String(stored, StandardCharsets.UTF_8);
        assertFalse(json.contains("districtValid"));
        assertFalse(json.contains("locationValid"));
        PlaceDto restored = assertInstanceOf(PlaceDto.class, serializer.deserialize(stored));
        assertEquals(1, restored.getStep());
    }

    @Test
    void existingSessionsWithComputedValidationPropertiesRemainReadable() {
        byte[] stored =
                """
                ["com.sport.service.dto.PlaceDto",{"step":3,"placeType":"SPORT_GROUND",
                "districtValid":true,"locationValid":true}]
                """
                        .getBytes(StandardCharsets.UTF_8);
        PlaceDto restored = assertInstanceOf(PlaceDto.class, serializer.deserialize(stored));
        assertEquals(3, restored.getStep());
        assertEquals(PlaceType.SPORT_GROUND, restored.getPlaceType());
    }

    @Test
    void searchFiltersIncludingAllSubdistrictsSurviveRoundTrip() {
        PlaceDto session = new PlaceDto();
        session.setStep(4);
        session.setDistrict(District.ZHELEZNODOROZHNYY);
        session.setSubdistrict(SubDistrict.ALL_SUBDISTRICTS);
        session.setPlaceType(PlaceType.SPORT_GROUND);
        session.setOutdoor(null);
        PlaceDto restored = (PlaceDto) serializer.deserialize(serializer.serialize(session));
        assertEquals(session, restored);
    }

    @Test
    void jsonExclusionDoesNotDisableRequestValidation() {
        PlaceDto request = new PlaceDto();
        request.setDistrict(District.ALL_DISTRICTS);
        request.setCoordinates("200, 400");
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator().validate(request);
            assertTrue(
                    violations.stream()
                            .anyMatch(v -> v.getPropertyPath().toString().equals("districtValid")));
            assertTrue(
                    violations.stream()
                            .anyMatch(v -> v.getPropertyPath().toString().equals("locationValid")));
        }
    }
}
