package com.sport.service.web.models.event;

import com.sport.service.entities.enums.common.District;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventEditRequest(
        @NotNull Long version,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 10000) String description,
        @NotNull LocalDate date,
        LocalTime time,
        @NotNull District district,
        @NotBlank @Size(max = 255) String address,
        @NotBlank @Size(max = 255) String placeName,
        @Size(max = 2000) String link,
        @Size(max = 100) String coordinates) {
    @AssertTrue(message = "Укажите конкретный район")
    public boolean isDistrictValid() {
        return district != District.ALL_DISTRICTS;
    }

    @AssertTrue(message = "Ссылка должна начинаться с https:// или http://")
    public boolean isLinkValid() {
        if (link == null || link.isBlank()) return true;
        try {
            URI uri = URI.create(link);
            return ("https".equalsIgnoreCase(uri.getScheme())
                            || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @AssertTrue(message = "Координаты: широта, долгота (например, 51.65, 39.15)")
    public boolean isCoordinatesValid() {
        if (coordinates == null || coordinates.isBlank()) return true;
        try {
            String[] parts = coordinates.split(",", -1);
            if (parts.length != 2) return false;
            double lat = Double.parseDouble(parts[0].strip());
            double lon = Double.parseDouble(parts[1].strip());
            return Double.isFinite(lat)
                    && Double.isFinite(lon)
                    && Math.abs(lat) <= 90
                    && Math.abs(lon) <= 180;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
