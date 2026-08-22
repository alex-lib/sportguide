package com.sport.service.services;

import com.sport.service.configurations.MinioService;
import com.sport.service.entities.Place;
import com.sport.service.repositories.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacePhotoMigrationService implements CommandLineRunner {
    private final PlaceRepository placeRepository;
    private final MinioService minioService;

    @Override
    public void run(String... args) {
        log.info("Starting photo migration from DB to MinIO...");
        List<Place> places = placeRepository.findAll();
        int migrated = 0;
        int skipped = 0;

        for (Place place : places) {
            if (place.getPhoto() == null || place.getPhoto().length == 0) {
                skipped++;
                continue;
            }

            if (place.getPhotoUrl() != null && !place.getPhotoUrl().isEmpty()) {
                skipped++;
                continue;
            }

            try {
                String objectName = "places/" + place.getId() + "/photo.jpg";
                minioService.uploadFileBytes(objectName, place.getPhoto(), "image/jpeg");
                place.setPhotoUrl(objectName);
                placeRepository.save(place);
                migrated++;
                log.info("Migrated photo for place: {}", place.getName());
            } catch (Exception e) {
                log.error("Failed to migrate photo for place {}: {}", place.getName(), e.getMessage());
            }
        }

        log.info("Photo migration completed. Migrated: {}, Skipped: {}, Total: {}", migrated, skipped, places.size());
    }
}
