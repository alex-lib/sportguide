package com.sport.service.dto;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDto {

    private District district;

    private Type type;

    private Boolean outdoor;

    private String name;

    private String address;

    private String description;

    private String webSite;

    private byte[] photo;

    private int step = 0;
}