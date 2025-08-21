package com.sport.service.components;
import com.sport.service.entities.place.Place;
import com.sport.service.entities.place.PlaceDistrict;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.repositories.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PlaceRepository placeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (placeRepository.count() == 0) {
            ClassPathResource imgFile = new ClassPathResource("photos/stadium.png");
            if (!imgFile.exists()) {
                System.err.println("Файл не найден: photos/stadium.png");
                return;
            }

            byte[] photoBytes;
            try (var in = imgFile.getInputStream()) {
                photoBytes = in.readAllBytes();
            }

            Place place = Place.builder()
                    .name("Центральный стадион профсоюзов")
                    .district(PlaceDistrict.CENTRALNYY)
                    .address("Студенческая улица, 12")
                    .description("Главный футбольный стадион города")
                    .webSite("https://fkfakel.ru/hall")
                    .outdoor(false)
                    .type(PlaceType.FOOTBALL_FIELD)
                    .photo(photoBytes)
                    .build();

            placeRepository.save(place);
        }
    }
}