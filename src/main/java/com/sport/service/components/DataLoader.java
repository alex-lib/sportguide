//package com.sport.service.components;
//import com.sport.service.entities.place.District;
//import com.sport.service.entities.place.Place;
//import com.sport.service.entities.place.Type;
//import com.sport.service.repositories.PlaceRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DataLoader implements CommandLineRunner {
//
//    private final PlaceRepository placeRepository;
//
//    @Override
//    public void run(String... args) {
//        try {
//            if (placeRepository.count() == 0) {
//                log.info("No places found in database, initializing with sample data...");
//
//                byte[] photoBytes = null;
//                ClassPathResource imgFile = new ClassPathResource("photos/stadium.png");
//                if (imgFile.exists()) {
//                    try (var in = imgFile.getInputStream()) {
//                        photoBytes = in.readAllBytes();
//                        log.info("Successfully loaded photo: {} bytes", photoBytes.length);
//                    } catch (Exception e) {
//                        log.warn("Failed to read photo file, creating place without photo", e);
//                    }
//                } else {
//                    log.warn("Photo file not found: photos/stadium.png, creating place without photo");
//                }
//
//                Place place = Place.builder()
//                        .name("Центральный стадион профсоюзов")
//                        .district(District.CENTRALNYY)
//                        .address("Студенческая улица, 12")
//                        .description("Главный футбольный стадион города")
//                        .webSite("https://fkfakel.ru/hall")
//                        .outdoor(false)
//                        .type(Type.FOOTBALL_FIELD)
//                        .photo(photoBytes)
//                        .build();
//
//                log.info("Saving place: {}", place.getName());
//                Place savedPlace = placeRepository.save(place);
//                log.info("Successfully saved place with ID: {}", savedPlace.getId());
//            } else {
//                log.info("Database already contains {} places, skipping initialization", placeRepository.count());
//            }
//        } catch (Exception e) {
//            log.error("DataLoader failed", e);
//        }
//    }
//}