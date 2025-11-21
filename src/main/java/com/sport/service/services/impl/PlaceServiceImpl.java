package com.sport.service.services.impl;

import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;
import com.sport.service.mappers.DistrictStringMapper;
import com.sport.service.mappers.OutdoorStringMapper;
import com.sport.service.mappers.PlaceTypeStringMapper;
import com.sport.service.mappers.SubDistrictStringMapper;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.services.PlaceService;
import com.sport.service.specifications.PlaceSpecification;
import com.sport.service.web.models.place.PlaceFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;

    @Transactional
    @Override
    public void create(PlaceDto dto) {
        placeRepository.save(placeMapper.placeDtoToPlace(dto));
    }

    @Override
    public boolean existsByName(String name) {
        return placeRepository.existsByName(name);
    }


    @Override
    public List<Place> findByDistrictAndPlaceTypeAndOutdoor(District district, PlaceType placeType, Boolean outdoor) {
        return placeRepository.findByDistrictAndPlaceTypeAndOutdoor(district, placeType, outdoor);
    }

    @Override
    public List<Place> findByDistrictAndPlaceType(District district, PlaceType placeType) {
        return placeRepository.findByDistrictAndPlaceType(district, placeType);
    }

    @Override
    public List<Place> findAll(PlaceFilter filter) {
        log.info("logs from findAll methjd from PlaceServiceImpl class");
        log.info("Filter = {}", filter);
        log.info("District = {}", DistrictStringMapper.districtStringToDistrictEnum(filter.getDistrict()));
        log.info("SubDistrict = {}", SubDistrictStringMapper.subDistrictStringToSubDistrictEnum(filter.getSubDistrict()));
        log.info("Outdoor = {}", OutdoorStringMapper.outdoorStringToOutdoorEnum(filter.getOutdoor()));
        log.info("PlaceType = {}", PlaceTypeStringMapper.placeTypeStringToPlaceTypeEnum(filter.getPlaceType()));
        Specification<Place> places = PlaceSpecification.withFilter(filter);
        log.info("Specification = {}", places);
        return placeRepository.findAll(PlaceSpecification.withFilter(filter));
    }

    @Override
    public Place findByName(String name) {
        return placeRepository.findByName(name);
    }

    @Override
    public List<Place> findByDistrictAndSubdistrictAndPlaceType(District district, SubDistrict subdistrict, PlaceType placeType) {
        return placeRepository.findByDistrictAndSubDistrictAndPlaceType(district, subdistrict, placeType);
    }

    @Override
    public List<Place> findByDistrictAndSubdistrictAndPlaceTypeAndOutdoor(District district, SubDistrict subdistrict, PlaceType placeType, Boolean outdoor) {
        return placeRepository.findByDistrictAndSubDistrictAndPlaceTypeAndOutdoor(district, subdistrict, placeType, outdoor);
    }

    @Override
    public List<Place> findAllByPlaceType(PlaceType placeType) {
        return placeRepository.findAllByPlaceType(placeType);
    }

    @Override
    public List<Place> findAllByPlaceTypeAndOutdoor(PlaceType placeType, Boolean outdoor) {
        return placeRepository.findAllByPlaceTypeAndOutdoor(placeType, outdoor);
    }

    @Transactional
    @Override
    public void deleteByName(String name) {
        Place place = placeRepository.findByName(name);
        if (place != null) placeRepository.delete(place);
    }
}