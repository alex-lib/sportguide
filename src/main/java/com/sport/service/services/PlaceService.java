package com.sport.service.services;

import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceType;

import java.util.List;

public interface PlaceService {

    List<Place> findByDistrict(District district);

    List<Place> findByType(List<Place> places, PlaceType placeType);

    List<Place> findByOutdoor(List<Place> places, Boolean outdoor);

    void create(PlaceDto dto);

    void deleteByName(String name);

    boolean existsByName(String name);

    List<Place> findByDistrictAndPlaceTypeAndOutdoor(District district, PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndPlaceType(District district, PlaceType placeType);
}