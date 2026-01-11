package com.sport.service.web.models.place;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceResponse {

    private String name;
    private String district;
    private String subDistrict;     // nullable
    private String address;
    private String description;     // nullable
    private String webSite;         // nullable
    private String outdoor;
    private String placeType;
    private byte[] photo;
    private String coordinates;     // nullable
}
