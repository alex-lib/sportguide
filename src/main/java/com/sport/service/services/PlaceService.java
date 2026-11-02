package com.sport.service.services;

import com.sport.service.configurations.MinioService;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Place;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.SubDistrict;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.store.notifications.Notification;
import com.sport.service.web.models.place.ListPlaceResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;
    private final MinioService minioService;
    private final ApplicationEventPublisher events;

    @Transactional
    @CacheEvict(value = "places", allEntries = true)
    public Long create(PlaceDto dto) {
        Place place = placeMapper.placeDtoToPlace(dto);
        place.setPhotoUrl(null);
        if (place.getCoordinates() != null && place.getCoordinates().isBlank())
            place.setCoordinates(null);
        placeRepository.save(place);
        if (dto.getPhoto() != null && dto.getPhoto().length > 0) {
            String objectName = "places/" + place.getId() + "/photo.jpg";
            minioService.uploadFileBytes(objectName, dto.getPhoto(), "image/jpeg");
            place.setPhotoUrl(objectName);
            placeRepository.save(place);
            log.info("Photo uploaded to MinIO for place '{}': {}", place.getName(), objectName);
        }
        events.publishEvent(
                Notification.builder()
                        .type(Notification.NotificationType.PLACE)
                        .message(
                                "📍 Новое место: <b>"
                                        + HtmlUtils.htmlEscape(place.getName())
                                        + "</b>\n"
                                        + "Посмотрите подробности в разделе «Места» приложения.")
                        .build());
        return place.getId();
    }

    @Transactional(readOnly = true)
    public PlaceDto getForEditing(Long id) {
        return placeMapper.placeToDto(requirePlace(id));
    }

    private Place requirePlace(Long id) {
        return placeRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Место не найдено"));
    }

    @Transactional
    @CacheEvict(value = "places", allEntries = true)
    public void update(Long id, PlaceDto dto) {
        Place place = requirePlace(id);
        place.setName(dto.getName());
        place.setDistrict(dto.getDistrict());
        place.setSubDistrict(dto.getSubdistrict());
        place.setPlaceType(dto.getPlaceType());
        place.setOutdoor(dto.getOutdoor());
        place.setAddress(dto.getAddress());
        place.setDescription(dto.getDescription());
        place.setWebSite(dto.getWebSite());
        place.setCoordinates(
                dto.getCoordinates() == null || dto.getCoordinates().isBlank()
                        ? null
                        : dto.getCoordinates());
        placeRepository.save(place);
    }

    @Transactional
    @CacheEvict(value = "places", allEntries = true)
    public void deleteById(Long id) {
        Place place = requirePlace(id);
        // The owning side must release join-table references before deleting the place.
        for (var coach : place.getCoaches()) {
            coach.getWorkPlaces().removeIf(workPlace -> id.equals(workPlace.getId()));
        }
        placeRepository.flush();
        placeRepository.delete(place);
        placeRepository.flush();
        if (place.getPhotoUrl() != null) minioService.deleteFile(place.getPhotoUrl());
    }

    public boolean existsByName(String name) {
        return placeRepository.existsByName(name);
    }

    public List<Place> findByDistrictAndPlaceTypeAndOutdoor(
            District district, PlaceType placeType, Boolean outdoor) {
        return placeRepository.findByDistrictAndPlaceTypeAndOutdoor(district, placeType, outdoor);
    }

    public List<Place> findByDistrictAndPlaceType(District district, PlaceType placeType) {
        return placeRepository.findByDistrictAndPlaceType(district, placeType);
    }

    @Cacheable(value = "places")
    public ListPlaceResponse findAll(
            String districtStr,
            String subDistrictStr,
            String outdoor,
            String placeType,
            String search) {
        District district;
        if (districtStr == null || districtStr.isEmpty() || districtStr.equals("ALL_DISTRICTS")) {
            district = null;
        } else {
            district = District.valueOf(districtStr);
        }

        SubDistrict subDistrict;
        if (subDistrictStr == null
                || subDistrictStr.isEmpty()
                || subDistrictStr.equals("ALL_SUBDISTRICTS")) {
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

        var result =
                placeRepository.findWithFilters(
                        district, subDistrict, outdoorVal, placeTypeEnum, search);
        return placeMapper.listPlaceToListPlaceResponse(result);
    }

    public Place findByName(String name) {
        return placeRepository.findByName(name);
    }

    public List<Place> findByDistrictAndSubDistrictAndPlaceType(
            District district, SubDistrict subDistrict, PlaceType placeType) {
        return placeRepository.findByDistrictAndSubDistrictAndPlaceType(
                district, subDistrict, placeType);
    }

    public List<Place> findByDistrictAndSubDistrictAndPlaceTypeAndOutdoor(
            District district, SubDistrict subDistrict, PlaceType placeType, Boolean outdoor) {
        return placeRepository.findByDistrictAndSubDistrictAndPlaceTypeAndOutdoor(
                district, subDistrict, placeType, outdoor);
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

    public byte[] getPhoto(String photoUrl) {
        log.info("Getting photo for URL: {}", photoUrl);
        if (photoUrl == null || photoUrl.isEmpty()) {
            log.warn("No photoUrl provided");
            return new byte[0];
        }
        try {
            byte[] photo = minioService.getFile(photoUrl);
            if (photo == null || photo.length == 0) {
                log.warn("No photo found in MinIO for URL: {}", photoUrl);
                return new byte[0];
            }
            return photo;
        } catch (Exception e) {
            log.error("Failed to get photo for URL {}: {}", photoUrl, e.getMessage());
            return new byte[0];
        }
    }

    @Transactional
    @CacheEvict(value = "places", allEntries = true)
    public String uploadPhoto(Long id, MultipartFile file) {
        Place place = requirePlace(id);
        String objectName = "places/" + id + "/" + java.util.UUID.randomUUID();
        minioService.uploadFile(objectName, file);
        String url = objectName;
        if (place != null) {
            place.setPhotoUrl(url);
            placeRepository.save(place);
        }
        log.info("Photo uploaded for place '{}': {}", id, objectName);
        return url;
    }

    @Transactional
    @CacheEvict(value = "places", allEntries = true)
    public void deletePhoto(Long id) {
        Place place = requirePlace(id);
        if (place != null && place.getPhotoUrl() != null) {
            minioService.deleteFile(place.getPhotoUrl());
            place.setPhotoUrl(null);
            placeRepository.save(place);
            log.info("Photo deleted for place '{}': {}", id, place.getPhotoUrl());
        }
    }
}
