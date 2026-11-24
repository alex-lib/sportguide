package com.sport.service.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sport.service.configurations.MinioService;
import com.sport.service.entities.Place;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.web.models.place.ListPlaceResponse;
import com.sport.service.web.models.place.PlaceResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.InvalidClassException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringJUnitConfig(PlaceCacheCompatibilityTest.Config.class)
class PlaceCacheCompatibilityTest {
    @Autowired PlaceService service;
    @Autowired PlaceRepository repository;
    @Autowired PlaceMapper mapper;
    @Autowired Map<String, byte[]> data;

    @BeforeEach
    void setup() {
        reset(repository, mapper);
        data.clear();
        var places = List.of(Place.builder().id(7L).name("Зал").build());
        when(repository.findWithFilters(null, null, null, null, null)).thenReturn(places);
        when(repository.findWithFilters(
                        isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(places, call.getArgument(5), 1));
        when(mapper.listPlaceToListPlaceResponse(places))
                .thenAnswer(
                        call ->
                                new ListPlaceResponse(
                                        List.of(
                                                PlaceResponse.builder()
                                                        .id(7L)
                                                        .name("Зал")
                                                        .build())));
    }

    // Synthetic empty lists serialized using the actual classes at 3c27eeb and c7659bf.
    private byte[] fixture(String name) throws Exception {
        try (var stream = getClass().getResourceAsStream("/cache/" + name + ".ser")) {
            assertNotNull(stream);
            return stream.readAllBytes();
        }
    }

    @Test
    void previousSerializedFormatsReproduceTheIncompatibility() throws Exception {
        var serializer = new JdkSerializationRedisSerializer();
        for (var name : List.of("places-before-pagination", "places-before-cache-version")) {
            var error =
                    assertThrows(
                            SerializationException.class,
                            () -> serializer.deserialize(fixture(name)));
            assertInstanceOf(
                    InvalidClassException.class, NestedExceptionUtils.getMostSpecificCause(error));
        }
    }

    @Test
    void oldMapAndPageEntriesAreIgnoredAndNewResponsesAreCached() throws Exception {
        data.put(
                "places::" + SimpleKeyGenerator.generateKey(null, null, null, null, null),
                fixture("places-before-pagination"));
        data.put(
                "places::" + SimpleKeyGenerator.generateKey(null, null, null, null, null, 0, 20),
                fixture("places-before-cache-version"));
        for (int i = 0; i < 2; i++) {
            var map = service.findAll(null, null, null, null, null);
            var page = service.findPage(null, null, null, null, null, 0, 20);
            assertEquals("Зал", map.getList().getFirst().getName());
            assertNull(map.getPage());
            assertEquals(0, page.getPage());
            assertEquals(20, page.getSize());
            assertEquals(1L, page.getTotalElements());
            assertEquals(7L, page.getList().getFirst().getId());
        }
        verify(repository, times(1)).findWithFilters(null, null, null, null, null);
        verify(repository, times(1))
                .findWithFilters(
                        isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
        assertEquals(
                2, data.keySet().stream().filter(key -> key.startsWith("places::v2:")).count());
    }

    @Test
    void changingCatalogStillEvictsBothMapAndPageCaches() {
        service.findAll(null, null, null, null, null);
        service.findPage(null, null, null, null, null, 0, 20);
        when(repository.findById(9L))
                .thenReturn(
                        java.util.Optional.of(Place.builder().id(9L).coaches(List.of()).build()));
        service.deleteById(9L);
        assertTrue(data.isEmpty());
        service.findAll(null, null, null, null, null);
        service.findPage(null, null, null, null, null, 0, 20);
        verify(repository, times(2)).findWithFilters(null, null, null, null, null);
        verify(repository, times(2))
                .findWithFilters(
                        isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Configuration
    @EnableCaching
    static class Config {
        @Bean
        Map<String, byte[]> data() {
            return new HashMap<>();
        }

        @Bean
        PlaceRepository repository() {
            return mock(PlaceRepository.class);
        }

        @Bean
        PlaceMapper mapper() {
            return mock(PlaceMapper.class);
        }

        @Bean
        PlaceService service(PlaceRepository repository, PlaceMapper mapper) {
            return new PlaceService(
                    repository,
                    mapper,
                    mock(MinioService.class),
                    mock(ApplicationEventPublisher.class));
        }

        @Bean
        RedisCacheManager cacheManager(Map<String, byte[]> data) {
            var writer = mock(RedisCacheWriter.class);
            when(writer.get(anyString(), any(byte[].class)))
                    .thenAnswer(
                            call ->
                                    data.get(
                                            new String(
                                                    call.getArgument(1, byte[].class),
                                                    StandardCharsets.UTF_8)));
            doAnswer(
                            call -> {
                                data.put(
                                        new String(
                                                call.getArgument(1, byte[].class),
                                                StandardCharsets.UTF_8),
                                        call.getArgument(2));
                                return null;
                            })
                    .when(writer)
                    .put(anyString(), any(byte[].class), any(byte[].class), any());
            doAnswer(
                            call -> {
                                data.clear();
                                return null;
                            })
                    .when(writer)
                    .clean(anyString(), any(byte[].class));
            return RedisCacheManager.builder(writer)
                    .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
                    .initialCacheNames(Set.of("places"))
                    .build();
        }
    }
}
