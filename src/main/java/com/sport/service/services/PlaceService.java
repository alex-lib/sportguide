package com.sport.service.services;

import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.web.models.place.ListPlaceResponse;
import com.sport.service.web.models.place.PlaceFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public ListPlaceResponse findAll(PlaceFilter filter) {
        log.info("findAll Places | district={}, subDistrict={}, outdoor={}, placeType={}",
                filter.getDistrict(), filter.getSubDistrict(), filter.getOutdoor(), filter.getPlaceType());
        District district;
        if (filter.getDistrict() == null || filter.getDistrict().isEmpty() || filter.getDistrict().equals("ALL_DISTRICTS")) {
            district = null;
        } else {
            district = District.valueOf(filter.getDistrict());
        }

        SubDistrict subDistrict;
        if (filter.getSubDistrict() == null || filter.getSubDistrict().isEmpty() || filter.getSubDistrict().equals("ALL_SUBDISTRICTS")) {
            subDistrict = null;
        } else {
            subDistrict = SubDistrict.valueOf(filter.getSubDistrict());
        }

        Boolean outdoor = null;
        if (filter.getOutdoor() != null && !filter.getOutdoor().isEmpty()) {
            outdoor = Boolean.parseBoolean(filter.getOutdoor());
        }

        PlaceType placeType;
        if (filter.getPlaceType() == null || filter.getPlaceType().isEmpty()) {
            placeType = null;
        } else {
            placeType = PlaceType.valueOf(filter.getPlaceType());
        }

        var result = placeRepository.findWithFilters(district, subDistrict, outdoor, placeType);
        log.info("findAll Places | resolved district={}, subDistrict={}, outdoor={}, placeType={} | found={} entities",
                district, subDistrict, outdoor, placeType, result.size());
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
    public void deleteByName(String name) {
        Place place = placeRepository.findByName(name);
        if (place != null) placeRepository.delete(place);
    }
}
