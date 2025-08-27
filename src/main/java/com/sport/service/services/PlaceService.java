package com.sport.service.services;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Type;
import java.util.List;

public interface PlaceService {

    List<Place> findByDistrict(District district);

    List<Place> findByType(List<Place> places, Type type);

    List<Place> findByOutdoor(List<Place> places, Boolean outdoor);

    void create(Place place);

    void deleteByName(String name);

    boolean existsByName(String name);
}