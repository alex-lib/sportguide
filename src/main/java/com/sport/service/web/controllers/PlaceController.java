package com.sport.service.web.controllers;

import com.sport.service.services.PlaceService;
import com.sport.service.web.models.place.ListPlaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListPlaceResponse getAllPlaces(
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String subDistrict,
        @RequestParam(required = false) String outdoor,
        @RequestParam(required = false) String placeType,
        @RequestParam(required = false) String search
    )  {
        return placeService.findAll(district, subDistrict, outdoor, placeType, search);
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> getPhoto(@RequestParam String photoUrl) {
        byte[] photo = placeService.getPhoto(photoUrl);
        if (photo == null || photo.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photo);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        String url = placeService.uploadPhoto(id, file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        placeService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }
}
