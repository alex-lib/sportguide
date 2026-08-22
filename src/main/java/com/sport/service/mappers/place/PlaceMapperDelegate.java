package com.sport.service.mappers.place;

import com.sport.service.configurations.MinioService;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Place;
import com.sport.service.mappers.string.DistrictStringMapper;
import com.sport.service.mappers.string.OutdoorStringMapper;
import com.sport.service.mappers.string.PlaceTypeStringMapper;
import com.sport.service.mappers.string.SubDistrictStringMapper;
import com.sport.service.web.models.place.ListPlaceResponse;
import com.sport.service.web.models.place.PlaceResponse;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public abstract class PlaceMapperDelegate implements PlaceMapper {
    private MinioService minioService;

    @Autowired
    public PlaceMapperDelegate(MinioService minioService) {
        this.minioService = minioService;
    }

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
                .photoUrl(dto.getPhotoUrl())
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

            String coordinates;
            if (place.getCoordinates() != null) {
                String[] coordinatesArray = place.getCoordinates().split(",");
                float latitude = Float.parseFloat(coordinatesArray[0].trim());
                float longitude = Float.parseFloat(coordinatesArray[1].trim());
                coordinates = String.format("https://maps.google.com/?q=%f,%f", latitude, longitude);
            } else {
                coordinates = "Координаты места не указаны";
            }

            String photoUrl = null;
            if (place.getPhotoUrl() != null) {
                photoUrl = minioService.getPresignedObjectUrl(place.getPhotoUrl());
            }

            placeResponses.add(PlaceResponse.builder()
                    .id(place.getId())
                    .name(place.getName())
                    .district(districtString)
                    .subDistrict(subDistrictString)
                    .address(place.getAddress())
                    .description(place.getDescription())
                    .webSite(place.getWebSite())
                    .outdoor(outdoorString)
                    .placeType(placeTypeString)
                    .coordinates(coordinates)
                    .photoUrl(photoUrl)
                    .build());
        }
        return new ListPlaceResponse(placeResponses);
    }
}