package com.sport.service.services;

import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.Subdistrict;

import java.util.List;

public interface PlaceService {

    void create(PlaceDto dto);

    void deleteByName(String name);

    boolean existsByName(String name);

    List<Place> findAllByPlaceType(PlaceType placeType);

    List<Place> findAllByPlaceTypeAndOutdoor(PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndSubdistrictAndPlaceTypeAndOutdoor(District district, Subdistrict subdistrict, PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndSubdistrictAndPlaceType(District district, Subdistrict subdistrict, PlaceType placeType);

    List<Place> findByDistrictAndPlaceTypeAndOutdoor(District district, PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndPlaceType(District district, PlaceType placeType);
}