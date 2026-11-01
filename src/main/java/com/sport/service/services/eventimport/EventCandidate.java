package com.sport.service.services.eventimport;

import com.sport.service.entities.enums.common.District;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class EventCandidate {
    public enum Source {
        KASSIR,
        FAKEL
    }

    private final Source source;
    private String externalId;
    private String link;
    private String name;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private String placeName;
    private String address;
    private District district;
    private final Map<String, String> collected = new LinkedHashMap<>();
    private final List<String> problems = new ArrayList<>();

    public EventCandidate(Source source, String externalId, String link) {
        this.source = source;
        this.externalId = externalId;
        this.link = link;
    }

    public List<String> missingFields() {
        List<String> missing = new ArrayList<>();
        if (blank(name)) missing.add("название");
        if (blank(description)) missing.add("описание");
        if (date == null) missing.add("дата");
        if (time == null) missing.add("время");
        if (blank(placeName)) missing.add("место");
        if (blank(address)) missing.add("адрес");
        if (district == null) missing.add("район");
        if (blank(link)) missing.add("ссылка");
        if (blank(externalId)) missing.add("идентификатор события источника");
        return missing;
    }

    public boolean complete() {
        return missingFields().isEmpty() && problems.isEmpty();
    }

    public boolean readyToImport() {
        return problems.isEmpty()
                && (complete()
                        || (source == Source.FAKEL && missingFields().equals(List.of("время"))));
    }

    public static boolean blank(String text) {
        return text == null || text.isBlank();
    }
}
