package com.sport.service.dto;
import com.sport.service.entities.place.PlaceDistrict;
import com.sport.service.entities.place.PlaceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDto {

    private PlaceDistrict district;

    private PlaceType type;

    private Boolean outdoor;

    private String name;

    private String address;

    private String description;

    private String webSite;

    private byte[] photo;

    private int step = 0;
}