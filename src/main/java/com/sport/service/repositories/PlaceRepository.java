package com.sport.service.repositories;

import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long>, JpaSpecificationExecutor<Place> {

    Place findByName(String name);

    boolean existsByName(String name);

    List<Place> findAllByPlaceType(PlaceType placeType);

    List<Place> findAllByPlaceTypeAndOutdoor(PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndSubDistrictAndPlaceTypeAndOutdoor(District district, SubDistrict subDistrict, PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndSubDistrictAndPlaceType(District district, SubDistrict subDistrict, PlaceType placeType);

    List<Place> findByDistrictAndPlaceTypeAndOutdoor(District district, PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndPlaceType(District district, PlaceType placeType);
}