package com.sport.service.services;

import com.sport.service.configurations.MinioService;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.web.models.place.ListPlaceResponse;
import io.minio.errors.ServerException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.XmlParserException;
import io.minio.errors.InternalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;
    private final MinioService minioService;

    @Transactional
    @CacheEvict(value = "places", allEntries = true)
    public void create(PlaceDto dto) {
        Place place = placeMapper.placeDtoToPlace(dto);
        placeRepository.save(place);
        if (dto.getPhoto() != null && dto.getPhoto().length > 0) {
            String objectName = "places/" + place.getId() + "/photo.jpg";
            minioService.uploadFileBytes(objectName, dto.getPhoto(), "image/jpeg");
            place.setPhotoUrl(objectName);
            placeRepository.save(place);
            log.info("Photo uploaded to MinIO for place '{}': {}", place.getName(), objectName);
        }
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
    public ListPlaceResponse findAll(String districtStr, String subDistrictStr, String outdoor, String placeType, String search) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
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
        ListPlaceResponse response = placeMapper.listPlaceToListPlaceResponse(result);
        return response;
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
        if (place != null) {
            if (place.getPhotoUrl() != null) {
                minioService.deleteFile(place.getPhotoUrl());
                log.info("Photo deleted from MinIO for place '{}': {}", name, place.getPhotoUrl());
            }
            placeRepository.delete(place);
        }
    }
}
