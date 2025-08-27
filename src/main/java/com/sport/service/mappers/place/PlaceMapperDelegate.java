package com.sport.service.mappers.place;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.Place;

public abstract class PlaceMapperDelegate implements PlaceMapper {

    @Override
    public Place placeDtoToPlace(PlaceDto dto) {
        return Place.builder()
                .district(dto.getDistrict())
                .type(dto.getType())
                .outdoor(dto.getOutdoor())
                .name(dto.getName())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .webSite(dto.getWebSite())
                .photo(dto.getPhoto())
                .build();
    }
}
