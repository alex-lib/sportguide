//package com.sport.service.services;
//
//import com.sport.service.configurations.MinioService;
//import com.sport.service.entities.Place;
//import com.sport.service.repositories.PlaceRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class PlacePhotoMigrationService implements CommandLineRunner {
//    private final PlaceRepository placeRepository;
//    private final MinioService minioService;
//
//    @Override
//    public void run(String... args) {
//        log.info("===========================================");
//        log.info("Starting photo migration from DB to MinIO...");
//        log.info("MinioService = {}", minioService);
//        log.info("PlaceRepository = {}", placeRepository);
//        log.info("===========================================");
//        List<Place> places;
//        try {
//            log.info("Fetching all places from database...");
//            places = placeRepository.findAll();
//            log.info("Fetched {} places from database", places.size());
//        } catch (Exception e) {
//            log.error("Failed to fetch places from database", e);
//            return;
//        }
//
//        int migrated = 0;
//        int skipped = 0;
//
//        for (Place place : places) {
//            log.info("Processing place: id={}, name='{}', hasPhoto={}, photoUrl={}",
//                    place.getId(), place.getName(),
//                    place.getPhoto() != null && place.getPhoto().length > 0,
//                    place.getPhotoUrl());
//
//            if (place.getPhoto() == null || place.getPhoto().length == 0) {
//                log.info("Skipping '{}' - no photo", place.getName());
//                skipped++;
//                continue;
//            }
//
//            if (place.getPhotoUrl() != null && !place.getPhotoUrl().isEmpty()) {
//                log.info("Skipping '{}' - already has photoUrl={}", place.getName(), place.getPhotoUrl());
//                skipped++;
//                continue;
//            }
//
//            try {
//                log.info("Uploading photo for place '{}' ({} bytes)...", place.getName(), place.getPhoto().length);
//                String objectName = "places/" + place.getId() + "/photo.jpg";
//                log.info("MinIO upload target: bucket='{}', object='{}'", "places", objectName);
//
//                MinioService ms = minioService;
//                log.info("Calling minioService.uploadFileBytes...");
//
//                ms.uploadFileBytes(objectName, place.getPhoto(), "image/jpeg");
//                log.info("Upload successful");
//
//                place.setPhotoUrl(objectName);
//                placeRepository.save(place);
//                migrated++;
//                log.info("Migrated photo for place: {}", place.getName());
//            } catch (Exception e) {
//                log.error("Failed to migrate photo for place '{}': {}", place.getName(), e.getMessage(), e);
//            }
//        }
//
//        log.info("===========================================");
//        log.info("Photo migration completed. Migrated: {}, Skipped: {}, Total: {}", migrated, skipped, places.size());
//        log.info("===========================================");
//    }
//}
