package com.sport.service.web.models.place;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceFilter {
    private String district;
    private String subDistrict;
    private String outdoor;
    private String placeType;
}