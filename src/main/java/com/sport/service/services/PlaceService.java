package com.sport.service.services;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceDistrict;
import com.sport.service.entities.place.PlaceType;
import java.util.List;

public interface PlaceService {

    List<Place> findByPlaceDistrict(PlaceDistrict placeDistrict);

    List<Place> findByPlaceType(List<Place> places, PlaceType placeType);

    List<Place> findByOutdoor(List<Place> places, Boolean outdoor);
}