package com.sport.service.web.models.place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceResponse implements Serializable {

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