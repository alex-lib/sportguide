package com.sport.service.web.controllers;

import com.sport.service.configurations.MinioService;
import com.sport.service.services.PlaceService;
import com.sport.service.web.models.place.ListPlaceResponse;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("api/places")
@RequiredArgsConstructor
@Slf4j
public class PlaceController {
    private final PlaceService placeService;
    private final MinioService minioService;

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        try {
            String objectName = "places/" + id + "/photo.jpg";
            byte[] photo = minioService.getFile(objectName);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(photo);
        } catch (Exception e) {
            log.warn("Photo not found for place {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListPlaceResponse getAllPlaces(
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String subDistrict,
        @RequestParam(required = false) String outdoor,
        @RequestParam(required = false) String placeType,
        @RequestParam(required = false) String search
    ) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        log.info("[API] GET /api/places | district={}, subDistrict={}, outdoor={}, placeType={}, search={}",
                district, subDistrict, outdoor, placeType, search);
        return placeService.findAll(district, subDistrict, outdoor, placeType, search);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        String objectName = "places/" + id + "/" + file.getOriginalFilename();
        minioService.uploadFile(objectName, file);
        String url = minioService.getFileUrl(objectName);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        String objectName = "places/" + id;
        minioService.deleteFile(objectName);
        return ResponseEntity.noContent().build();
    }
}
