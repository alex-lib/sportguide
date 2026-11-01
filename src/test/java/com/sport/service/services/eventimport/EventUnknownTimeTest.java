package com.sport.service.services.eventimport;

import static org.assertj.core.api.Assertions.assertThat;

import com.sport.service.entities.Event;
import com.sport.service.entities.enums.common.District;
import com.sport.service.mappers.event.EventMapper;
import com.sport.service.services.NotificationCreatorService;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.time.LocalDate;
import java.util.List;

class EventUnknownTimeTest {
    @Test
    void unknownTimeIsShownInApplicationAndNotificationWithoutInventingMidnight() {
        Event event =
                Event.builder()
                        .id(1L)
                        .name("Факел — ЦСКА")
                        .description("Турнир")
                        .date(LocalDate.of(2027, 3, 14))
                        .district(District.SOVETSKYY)
                        .build();
        var response =
                Mappers.getMapper(EventMapper.class).listEventToListEventResponse(List.of(event));
        assertThat(response.getList().getFirst().getTime()).isEqualTo("Время уточняется");
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode(TemplateMode.TEXT);
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        String notification = new NotificationCreatorService(engine).createEventNotification(event);
        assertThat(notification)
                .startsWith("<i>Факел — ЦСКА</i>")
                .contains("Время уточняется")
                .doesNotContain("00:00", "Скоро домашний матч");
    }
}
