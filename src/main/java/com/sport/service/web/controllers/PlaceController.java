package com.sport.service.web.controllers;

import com.sport.service.dto.PlaceDto;
import com.sport.service.services.PlaceService;
import com.sport.service.web.models.place.ListPlaceResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    public Map<String, Boolean> permissions(Authentication authentication) {
        return Map.of(
                "canManage",
                authentication.getAuthorities().stream()
                        .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlaceDto getForEditing(@PathVariable Long id) {
        return placeService.getForEditing(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> create(@Valid @RequestBody PlaceDto request) {
        return Map.of("id", placeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable Long id, @Valid @RequestBody PlaceDto request) {
        placeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        placeService.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
    public ListPlaceResponse getAllPlaces(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String subDistrict,
            @RequestParam(required = false) String outdoor,
            @RequestParam(required = false) String placeType,
            @RequestParam(required = false) String search) {
        return placeService.findAll(district, subDistrict, outdoor, placeType, search);
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> getPhoto(@RequestParam String photoUrl) {
        byte[] photo = placeService.getPhoto(photoUrl);
        if (photo == null || photo.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photo);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
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
