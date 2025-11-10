package com.sport.service.services;

import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.entities.place.Subdistrict;

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