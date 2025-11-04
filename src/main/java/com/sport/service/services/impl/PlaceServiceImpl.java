package com.sport.service.services.impl;

import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.entities.place.Subdistrict;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.services.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<Place> findByDistrictAndSubdistrictAndPlaceType(District district, Subdistrict subdistrict, PlaceType placeType) {
        return placeRepository.findByDistrictAndSubdistrictAndPlaceType(district, subdistrict, placeType);
    }

    @Override
    public List<Place> findByDistrictAndSubdistrictAndPlaceTypeAndOutdoor(District district, Subdistrict subdistrict, PlaceType placeType, Boolean outdoor) {
        return placeRepository.findByDistrictAndSubdistrictAndPlaceTypeAndOutdoor(district, subdistrict, placeType, outdoor);
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