package com.sport.service.repositories;

import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    Place findByName(String name);

    boolean existsByName(String name);

    List<Place> findAllByDistrict(District district);

    List<Place> findByDistrictAndTypeAndOutdoor(District district, Type type, Boolean outdoor);

    List<Place> findByDistrictAndType(District district, Type type);
}