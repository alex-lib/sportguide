package com.sport.service.services.impl;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceDistrict;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.services.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;

    @Override
    public List<Place> findByPlaceDistrict(PlaceDistrict placeDistrict) {
        return placeDistrict == PlaceDistrict.ALL_DISTRICTS
                ? placeRepository.findAll()
                : placeRepository.findAll().stream()
                .filter(place -> place.getDistrict() == placeDistrict)
                .toList();
    }

    @Override
    public List<Place> findByPlaceType(List<Place> places, PlaceType placeType) {
        return places.stream()
                .filter(place -> place.getType() == placeType)
                .toList();
    }

    @Override
    public List<Place> findByOutdoor(List<Place> places, Boolean outdoor) {
        return places.stream()
                .filter(place -> place.getOutdoor() == outdoor)
                .toList();
    }
}