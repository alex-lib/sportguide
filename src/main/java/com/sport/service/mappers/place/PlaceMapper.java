package com.sport.service.mappers.place;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Place;
import com.sport.service.web.models.place.ListPlaceResponse;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@DecoratedWith(PlaceMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlaceMapper {

    Place placeDtoToPlace(PlaceDto dto);

    default ListPlaceResponse listPlaceToListPlaceResponse(List<Place> places) {
        return null;
    }
}