package com.sport.service.repositories;

import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.entities.place.Subdistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    Place findByName(String name);

    boolean existsByName(String name);

    List<Place> findAllByPlaceType(PlaceType placeType);

    List<Place> findAllByPlaceTypeAndOutdoor(PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndSubdistrictAndPlaceTypeAndOutdoor(District district, Subdistrict subdistrict, PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndSubdistrictAndPlaceType(District district, Subdistrict subdistrict, PlaceType placeType);

    List<Place> findByDistrictAndPlaceTypeAndOutdoor(District district, PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndPlaceType(District district, PlaceType placeType);
}