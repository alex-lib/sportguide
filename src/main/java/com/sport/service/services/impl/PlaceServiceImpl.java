package com.sport.service.services.impl;

import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.Type;
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

    @Transactional
    @Override
    public void create(Place place) {
            placeRepository.save(place);
    }

    @Override
    public boolean existsByName(String name) {
        return placeRepository.existsByName(name);
    }

    @Transactional
    @Override
    public void deleteByName(String name) {
        Place place = placeRepository.findByName(name);
        if (place != null) {
            placeRepository.delete(place);
        }
    }

    @Override
    public List<Place> findByDistrict(District district) {
        if (district == District.ALL_DISTRICTS) {
            return placeRepository.findAll();
        } else {
            return placeRepository.findAllByDistrict(district);
//            return placeRepository.findAll().stream()
//            .filter(place -> place.getDistrict() == district)
//            .toList();
        }
    }

    @Override
    public List<Place> findByType(List<Place> places, Type type) {
        return places.stream()
                .filter(place -> place.getType() == type)
                .toList();
    }

    @Override
    public List<Place> findByOutdoor(List<Place> places, Boolean outdoor) {
        return places.stream()
                .filter(place -> place.getOutdoor() == outdoor)
                .toList();
    }
}