//package com.sport.service.services;
//
//import com.sport.service.configurations.MinioService;
//import com.sport.service.entities.Place;
//import com.sport.service.repositories.PlaceRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
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
//    @Value("${telegram.bot.token}")
//    private String botToken;
//
//    @Override
//    public void run(String... args) {
//        log.info("================================================");
//        log.info("Starting photo migration from DB to MinIO...");
//        log.info("================================================");
//
//        List<Place> places = placeRepository.findAll();
//        log.info("Fetched {} places from database", places.size());
//
//        int migrated = 0;
//        int skippedExisting = 0;
//        int skippedNoPhoto = 0;
//        int failed = 0;
//
//        for (Place place : places) {
//            boolean hasBlobPhoto = place.getPhoto() != null && place.getPhoto().length > 0;
//            boolean hasMinioUrl = place.getPhotoUrl() != null && !place.getPhotoUrl().isEmpty();
//
//            if (hasMinioUrl) {
//                log.info("[SKIP] id={} - already has MinIO URL: {}", place.getId(), place.getPhotoUrl());
//                skippedExisting++;
//                continue;
//            }
//
//            if (!hasBlobPhoto) {
//                log.info("[SKIP] id={} - no BLOB photo", place.getId());
//                skippedNoPhoto++;
//                continue;
//            }
//
//            try {
//                log.info("[UPLOAD] id={} name='{}' photo={} bytes",
//                    place.getId(), place.getName(), place.getPhoto().length);
//
//                String objectName = "places/" + place.getId() + "/photo.jpg";
//                minioService.uploadFileBytes(objectName, place.getPhoto(), "image/jpeg");
//
//                place.setPhotoUrl(objectName);
//                placeRepository.save(place);
//
//                log.info("[OK] id={} - migrated", place.getId());
//                migrated++;
//            } catch (Exception e) {
//                log.error("[FAIL] id={} name='{}': {}", place.getId(), place.getName(), e.getMessage());
//                failed++;
//            }
//        }
//
//        log.info("================================================");
//        log.info("Migration completed:");
//        log.info("  Migrated: {}", migrated);
//        log.info("  Skipped (existing MinIO URLs): {}", skippedExisting);
//        log.info("  Skipped (no blob photos): {}", skippedNoPhoto);
//        log.info("  Failed: {}", failed);
//        log.info("  Total: {}", places.size());
//        log.info("================================================");
//    }
//}
