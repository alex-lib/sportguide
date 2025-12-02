package com.sport.service.dto;

import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlaceDto {
    private District district;
    private SubDistrict subdistrict;
    private PlaceType placeType;
    private Boolean outdoor;
    private String name;
    private String address;
    private String description;
    private String webSite;
    @ToString.Exclude
    private byte[] photo;
    private String coordinates;
    private int step = 0;
}