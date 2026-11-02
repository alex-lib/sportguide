package com.sport.service.dto;

import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.net.URI;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlaceDto {
    @NotNull private District district;
    private SubDistrict subdistrict;
    @NotNull private PlaceType placeType;
    @NotNull private Boolean outdoor;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String address;

    @Size(max = 10000)
    private String description;

    @Size(max = 2000)
    private String webSite;

    private String photoUrl;
    @ToString.Exclude private byte[] photo;

    @Size(max = 100)
    private String coordinates;

    private int step = 0;

    @AssertTrue(message = "Укажите конкретный район и подрайон")
    public boolean isDistrictValid() {
        return district != District.ALL_DISTRICTS && subdistrict != SubDistrict.ALL_SUBDISTRICTS;
    }

    @AssertTrue(message = "Проверьте ссылку и координаты: широта, долгота")
    public boolean isLocationValid() {
        try {
            if (webSite != null && !webSite.isBlank() && !"-".equals(webSite)) {
                URI uri = URI.create(webSite);
                if (!("https".equalsIgnoreCase(uri.getScheme())
                                || "http".equalsIgnoreCase(uri.getScheme()))
                        || uri.getHost() == null) return false;
            }
            if (coordinates == null || coordinates.isBlank()) return true;
            String[] parts = coordinates.split(",", -1);
            if (parts.length != 2) return false;
            double lat = Double.parseDouble(parts[0].strip());
            double lon = Double.parseDouble(parts[1].strip());
            return Double.isFinite(lat)
                    && Double.isFinite(lon)
                    && Math.abs(lat) <= 90
                    && Math.abs(lon) <= 180;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
