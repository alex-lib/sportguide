package com.sport.service.mappers.coach;

import static org.junit.jupiter.api.Assertions.*;

import com.sport.service.entities.Coach;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.coach.Sex;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.mappers.place.PlaceMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Locale;

@SpringJUnitConfig(CoachWorkPlacesTest.Config.class)
class CoachWorkPlacesTest {
    @Autowired CoachMapper mapper;

    @Test
    void editingPreservesMultipleSpecialtiesAndFullProfileText() {
        Coach original = coach(List.of());
        original.setSportTypes(
                List.of(
                        SportType.STRETCHING,
                        SportType.FITNESS,
                        SportType.GROUP_TRAININGS,
                        SportType.POLE_DANCE));
        original.setDescription("Специализация и спортивные достижения. ".repeat(12));
        original.setEducation("Образовательная организация — квалификация (2022).\n".repeat(8));
        original.setMonthsForSubscriptionToBeCoach((short) 120);

        var request = mapper.coachToRequest(original);
        assertTrue(request.isClassificationValid());
        var edited =
                mapper.coachRequestToCoach(
                        request, null, original.getSubscriber(), List.of(), List.of());
        var response = mapper.listCoachToListCoachResponse(List.of(edited)).getList().getFirst();

        assertEquals(original.getSportTypes(), edited.getSportTypes());
        assertEquals(
                List.of("Растяжка", "Фитнес", "Групповые тренировки", "Спортивный пилон"),
                response.getSportTypes());
        assertEquals(original.getDescription(), response.getDescription());
        assertEquals(original.getEducation(), response.getEducation());
    }

    @Test
    void profileIncludesSexStoredInTheDatabase() {
        Coach coach = coach(List.of());
        assertEquals(
                "Мужчина",
                mapper.listCoachToListCoachResponse(List.of(coach)).getList().getFirst().getSex());
        coach.setSex(Sex.FEMALE);
        assertEquals(
                "Женщина",
                mapper.listCoachToListCoachResponse(List.of(coach)).getList().getFirst().getSex());
        coach.setSex(null);
        assertNull(
                mapper.listCoachToListCoachResponse(List.of(coach)).getList().getFirst().getSex());
    }

    @Test
    void mapsLinkedPlacesWithCoordinatesAndKeepsLegacyNames() {
        Place first =
                Place.builder()
                        .district(District.LENINSKYY)
                        .outdoor(false)
                        .placeType(PlaceType.GYM)
                        .id(173L)
                        .name("Зал")
                        .address("ул. Бакунина, 45А")
                        .coordinates("51.671105, 39.185911")
                        .build();
        Place second =
                Place.builder()
                        .district(District.LENINSKYY)
                        .outdoor(false)
                        .placeType(PlaceType.GYM)
                        .id(174L)
                        .name("Зал")
                        .coordinates("51.7, 39.3")
                        .build();
        var response =
                mapper.listCoachToListCoachResponse(List.of(coach(List.of(first, second))))
                        .getList()
                        .getFirst();
        assertEquals(List.of("Зал", "Зал"), response.getWorkPlacesNames());
        assertEquals(
                List.of(173L, 174L),
                response.getWorkPlaces().stream().map(p -> p.getId()).toList());
        assertEquals("ул. Бакунина, 45А", response.getWorkPlaces().getFirst().getAddress());
        assertTrue(
                response.getWorkPlaces()
                        .getFirst()
                        .getCoordinates()
                        .startsWith("https://maps.google.com/?q=51.671"));
        String[] coordinates =
                response.getWorkPlaces().get(1).getCoordinates().split("q=")[1].split(",");
        assertEquals(51.7, Double.parseDouble(coordinates[0]), 0.00001);
        assertEquals(39.3, Double.parseDouble(coordinates[1]), 0.00001);
    }

    @Test
    @ResourceLock("java.util.Locale.default")
    void coordinatesUseDecimalDotsWithRussianLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("ru-RU"));
            Place place =
                    Place.builder()
                            .id(1L)
                            .name("Зал")
                            .district(District.LENINSKYY)
                            .outdoor(false)
                            .placeType(PlaceType.GYM)
                            .coordinates("51.5, 39.25")
                            .build();
            var response =
                    mapper.listCoachToListCoachResponse(List.of(coach(List.of(place))))
                            .getList()
                            .getFirst();
            assertEquals(
                    "https://maps.google.com/?q=51.500000,39.250000",
                    response.getWorkPlaces().getFirst().getCoordinates());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void handlesCoachWithoutWorkPlacesAndPlaceWithoutCoordinates() {
        var empty =
                mapper.listCoachToListCoachResponse(List.of(coach(List.of()))).getList().getFirst();
        assertTrue(empty.getWorkPlaces().isEmpty());
        var missing =
                mapper.listCoachToListCoachResponse(
                                List.of(
                                        coach(
                                                List.of(
                                                        Place.builder()
                                                                .district(District.LENINSKYY)
                                                                .outdoor(false)
                                                                .placeType(PlaceType.GYM)
                                                                .id(1L)
                                                                .name("Зал")
                                                                .build()))))
                        .getList()
                        .getFirst();
        assertEquals(
                "Координаты места не указаны", missing.getWorkPlaces().getFirst().getCoordinates());
    }

    private Coach coach(List<Place> places) {
        Subscriber subscriber = new Subscriber();
        subscriber.setUsername("testcoach");
        return Coach.builder()
                .id(1L)
                .name("Тренер")
                .subscriber(subscriber)
                .sex(Sex.MALE)
                .sportTypes(List.of(SportType.FITNESS))
                .workPlaces(places)
                .build();
    }

    @Configuration
    @ComponentScan(basePackageClasses = {CoachMapper.class, PlaceMapper.class})
    static class Config {}
}
