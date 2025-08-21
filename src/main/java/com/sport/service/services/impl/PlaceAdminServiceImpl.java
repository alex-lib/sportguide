package com.sport.service.services.impl;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceDistrict;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.services.PlaceAdminService;
import com.sport.service.services.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceAdminServiceImpl implements PlaceAdminService {

    private final PlaceService placeService;

    private final PlaceRepository placeRepository;

    @Transactional
    @Override
    public void createPlace(Place place) {
        placeRepository.save(place);
    }

    @Transactional
    @Override
    public void deletePlaceByName(String name) {
        Place place = placeRepository.findByName(name);
        if (place != null) {
            placeRepository.delete(place);
        }
    }

    @Override
    public List<Place> findByPlaceDistrict(PlaceDistrict placeDistrict) {
        return placeService.findByPlaceDistrict(placeDistrict);
    }

    @Override
    public List<Place> findByPlaceType(List<Place> places, PlaceType placeType) {
        return placeService.findByPlaceType(places, placeType);
    }

    @Override
    public List<Place> findByOutdoor(List<Place> places, Boolean outdoor) {
        return placeService.findByOutdoor(places, outdoor);
    }
}