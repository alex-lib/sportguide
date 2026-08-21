package com.sport.service.services;

import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.web.models.place.ListPlaceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;

    @Transactional
    @CacheEvict(value = "places", allEntries = true)
    public void create(PlaceDto dto) {
        placeRepository.save(placeMapper.placeDtoToPlace(dto));
    }

    public boolean existsByName(String name) {
        return placeRepository.existsByName(name);
    }

    public List<Place> findByDistrictAndPlaceTypeAndOutdoor(District district, PlaceType placeType, Boolean outdoor) {
        return placeRepository.findByDistrictAndPlaceTypeAndOutdoor(district, placeType, outdoor);
    }

    public List<Place> findByDistrictAndPlaceType(District district, PlaceType placeType) {
        return placeRepository.findByDistrictAndPlaceType(district, placeType);
    }

    @Cacheable(value = "places")
    public ListPlaceResponse findAll(String districtStr, String subDistrictStr, String outdoor, String placeType, String search) {
        log.info("findAll Places | district={}, subDistrict={}, outdoor={}, placeType={}, search={}",
                districtStr, subDistrictStr, outdoor, placeType, search);
        District district;
        if (districtStr == null || districtStr.isEmpty() || districtStr.equals("ALL_DISTRICTS")) {
            district = null;
        } else {
            district = District.valueOf(districtStr);
        }

        SubDistrict subDistrict;
        if (subDistrictStr == null || subDistrictStr.isEmpty() || subDistrictStr.equals("ALL_SUBDISTRICTS")) {
            subDistrict = null;
        } else {
            subDistrict = SubDistrict.valueOf(subDistrictStr);
        }

        Boolean outdoorVal = null;
        if (outdoor != null && !outdoor.isEmpty()) {
            outdoorVal = Boolean.parseBoolean(outdoor);
        }

        PlaceType placeTypeEnum;
        if (placeType == null || placeType.isEmpty()) {
            placeTypeEnum = null;
        } else {
            placeTypeEnum = PlaceType.valueOf(placeType);
        }

        var result = placeRepository.findWithFilters(district, subDistrict, outdoorVal, placeTypeEnum, search);
        log.info("findAll Places | resolved district={}, subDistrict={}, outdoor={}, placeType={}, search={} | found={} entities",
                district, subDistrict, outdoorVal, placeTypeEnum, search, result.size());
        return placeMapper.listPlaceToListPlaceResponse(result);
    }

    public Place findByName(String name) {
        return placeRepository.findByName(name);
    }

    public List<Place> findByDistrictAndSubDistrictAndPlaceType(District district, SubDistrict subDistrict, PlaceType placeType) {
        return placeRepository.findByDistrictAndSubDistrictAndPlaceType(district, subDistrict, placeType);
    }

    public List<Place> findByDistrictAndSubDistrictAndPlaceTypeAndOutdoor(District district, SubDistrict subDistrict, PlaceType placeType, Boolean outdoor) {
        return placeRepository.findByDistrictAndSubDistrictAndPlaceTypeAndOutdoor(district, subDistrict, placeType, outdoor);
    }

    public List<Place> findAllByPlaceType(PlaceType placeType) {
        return placeRepository.findAllByPlaceType(placeType);
    }

    public List<Place> findAllByPlaceTypeAndOutdoor(PlaceType placeType, Boolean outdoor) {
        return placeRepository.findAllByPlaceTypeAndOutdoor(placeType, outdoor);
    }

    @Transactional
    @CacheEvict(value = "places", allEntries = true)
    public void deleteByName(String name) {
        Place place = placeRepository.findByName(name);
        if (place != null) placeRepository.delete(place);
    }
}