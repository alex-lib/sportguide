package com.sport.service.components;

import com.sport.service.entities.place.District;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.Type;
import com.sport.service.repositories.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PlaceRepository placeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (placeRepository.count() == 0) {
            log.info("No places found in database, initializing with sample data...");

            ClassPathResource imgFile = new ClassPathResource("photos/stadium.png");
            if (!imgFile.exists()) {
                log.error("Photo file not found: photos/stadium.png");
                return;
            }

            byte[] photoBytes;
            try (var in = imgFile.getInputStream()) {
                photoBytes = in.readAllBytes();
                log.info("Successfully loaded photo: {} bytes", photoBytes.length);
            } catch (Exception e) {
                log.error("Failed to read photo file", e);
                return;
            }

            Place place = Place.builder()
                    .name("Центральный стадион профсоюзов")
                    .district(District.CENTRALNYY)
                    .address("Студенческая улица, 12")
                    .description("Главный футбольный стадион города")
                    .webSite("https://fkfakel.ru/hall")
                    .outdoor(false)
                    .type(Type.FOOTBALL_FIELD)
                    .photo(photoBytes)
                    .build();

            log.info("Saving place: {}", place.getName());
            Place savedPlace = placeRepository.save(place);
            log.info("Successfully saved place with ID: {}", savedPlace.getId());
        } else {
            log.info("Database already contains {} places, skipping initialization", placeRepository.count());
        }
    }
}