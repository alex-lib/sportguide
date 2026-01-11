package com.sport.service.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.module.kotlin.ExtensionsKt.jacksonObjectMapper;

@Configuration
public class ObjectMapperConfiguration {

    @Bean
    public ObjectMapper objectMapper () {
        return jacksonObjectMapper().findAndRegisterModules();
    }
}