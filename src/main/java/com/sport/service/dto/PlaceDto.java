package com.sport.service.dto;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.PlaceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDto {
    private District district;
    private PlaceType placeType;
    private Boolean outdoor;
    private String name;
    private String address;
    private String description;
    private String webSite;
    private byte[] photo;
    private String coordinates;
    private int step = 0;
}