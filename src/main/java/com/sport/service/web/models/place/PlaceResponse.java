package com.sport.service.web.models.place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceResponse {

    private Long id;
    private String name;
    private String district;
    private String subDistrict;
    private String address;
    private String description;
    private String webSite;
    private String outdoor;
    private String placeType;
    private String coordinates;
    private String photoUrl;
}