package com.sport.service.mappers.place;

import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Place;
import com.sport.service.mappers.string.DistrictStringMapper;
import com.sport.service.mappers.string.OutdoorStringMapper;
import com.sport.service.mappers.string.PlaceTypeStringMapper;
import com.sport.service.mappers.string.SubDistrictStringMapper;
import com.sport.service.web.models.place.ListPlaceResponse;
import com.sport.service.web.models.place.PlaceResponse;

import java.util.ArrayList;
import java.util.List;

public abstract class PlaceMapperDelegate implements PlaceMapper {

    @Override
    public Place placeDtoToPlace(PlaceDto dto) {
        return Place.builder()
                .name(dto.getName())
                .district(dto.getDistrict())
                .subDistrict(dto.getSubdistrict())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .webSite(dto.getWebSite())
                .outdoor(dto.getOutdoor())
                .placeType(dto.getPlaceType())
                .photo(dto.getPhoto())
                .coordinates(dto.getCoordinates())
                .build();
    }

    @Override
    public ListPlaceResponse listPlaceToListPlaceResponse(List<Place> places) {
        List<PlaceResponse> placeResponses = new ArrayList<>();

        for (Place place : places) {
            String districtString = DistrictStringMapper.districtEnumToDistrictString(place.getDistrict());
            String subDistrictString = SubDistrictStringMapper.subDistrictEnumToSubDistrictString(place.getSubDistrict());
            String outdoorString = OutdoorStringMapper.outdoorEnumToOutdoorString(place.getOutdoor());
            String placeTypeString = PlaceTypeStringMapper.placeTypeEnumToPlaceTypeString(place.getPlaceType());
            String coordinates = buildMapLink(place.getCoordinates());

            placeResponses.add(new PlaceResponse(
                    place.getName(),
                    districtString,
                    subDistrictString,
                    place.getAddress(),
                    place.getDescription(),
                    place.getWebSite(),
                    outdoorString,
                    placeTypeString,
                    place.getPhoto(),
                    coordinates));
        }
        return new ListPlaceResponse(placeResponses);
    }

    private static String buildMapLink(String rawCoordinates) {
        if (rawCoordinates == null || rawCoordinates.equals("-") || !rawCoordinates.contains(",")) {
            return "Координаты места не указаны";
        }
        try {
            String[] parts = rawCoordinates.split(",");
            float latitude = Float.parseFloat(parts[0].trim());
            float longitude = Float.parseFloat(parts[1].trim());
            return String.format("https://maps.google.com/?q=%f,%f", latitude, longitude);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return "Координаты места не указаны";
        }
    }
}