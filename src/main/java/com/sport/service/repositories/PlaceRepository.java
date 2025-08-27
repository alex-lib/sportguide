package com.sport.service.repositories;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("SELECT p FROM Place p WHERE p.name = :name")
    Place findByName(String name);

    @Query("SELECT COUNT(p) > 0 FROM Place p WHERE p.name = :name")
    boolean existsByName(String name);

    @Query("SELECT p FROM Place p WHERE p.district = :district")
    List<Place> findAllByDistrict(District district);
}