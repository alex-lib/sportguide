package com.sport.service.services;
import com.sport.service.entities.place.Place;

public interface PlaceAdminService extends PlaceService {

    void createPlace (Place place);

    void deletePlaceByName(String name);
}