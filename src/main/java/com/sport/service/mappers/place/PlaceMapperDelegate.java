package com.sport.service.mappers.place;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.Place;

public abstract class PlaceMapperDelegate implements PlaceMapper {

    @Override
    public Place placeDtoToPlace(PlaceDto dto) {
        return Place.builder()
                .name(dto.getName())
                .district(dto.getDistrict())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .webSite(dto.getWebSite())
                .outdoor(dto.getOutdoor())
                .type(dto.getType())
                .photo(dto.getPhoto())
                .build();
    }
}
