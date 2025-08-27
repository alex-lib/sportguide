package com.sport.service.mappers.place;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.Place;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@DecoratedWith(PlaceMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlaceMapper {

    Place placeDtoToPlace(PlaceDto dto);
}