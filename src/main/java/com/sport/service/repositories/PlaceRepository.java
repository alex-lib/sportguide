package com.sport.service.repositories;

import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    Place findByName(String name);

    boolean existsByName(String name);

    @Query("""
        SELECT p FROM Place p
        WHERE (:district IS NULL OR p.district = :district)
          AND (:subDistrict IS NULL OR p.subDistrict = :subDistrict)
          AND (:outdoor IS NULL OR p.outdoor = :outdoor)
          AND (:placeType IS NULL OR p.placeType = :placeType)
          AND (:search IS NULL
                OR LOWER(p.name) LIKE CONCAT('%', CAST(:search AS String), '%')
                OR LOWER(p.description) LIKE CONCAT('%', CAST(:search AS String), '%')
                OR LOWER(p.address) LIKE CONCAT('%', CAST(:search AS String), '%'))
        """)
    List<Place> findWithFilters(
            @Param("district") District district,
            @Param("subDistrict") SubDistrict subDistrict,
            @Param("outdoor") Boolean outdoor,
            @Param("placeType") PlaceType placeType,
            @Param("search") String search
    );

    List<Place> findAllByPlaceType(PlaceType placeType);

    List<Place> findAllByPlaceTypeAndOutdoor(PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndSubDistrictAndPlaceTypeAndOutdoor(District district, SubDistrict subDistrict, PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndSubDistrictAndPlaceType(District district, SubDistrict subDistrict, PlaceType placeType);

    List<Place> findByDistrictAndPlaceTypeAndOutdoor(District district, PlaceType placeType, Boolean outdoor);

    List<Place> findByDistrictAndPlaceType(District district, PlaceType placeType);
}
