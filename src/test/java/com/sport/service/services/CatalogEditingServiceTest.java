package com.sport.service.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sport.service.configurations.MinioService;
import com.sport.service.dto.PlaceDto;
import com.sport.service.entities.Coach;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.coach.Sex;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.mappers.coach.CoachMapperDelegate;
import com.sport.service.mappers.place.PlaceMapper;
import com.sport.service.repositories.CoachRepository;
import com.sport.service.repositories.PlaceRepository;
import com.sport.service.web.models.coach.CoachRequest;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class CatalogEditingServiceTest {
    private final PlaceRepository places = mock(PlaceRepository.class);
    private final CoachRepository coaches = mock(CoachRepository.class);
    private final MinioService storage = mock(MinioService.class);
    private final SubscriberService subscribers = mock(SubscriberService.class);
    private final PlaceService placeService =
            new PlaceService(
                    places,
                    mock(PlaceMapper.class),
                    storage,
                    mock(ApplicationEventPublisher.class));
    private final CoachService coachService =
            new CoachService(
                    coaches,
                    subscribers,
                    placeService,
                    mock(TrainingProgramService.class),
                    storage,
                    new CoachMapperDelegate() {},
                    mock(ApplicationEventPublisher.class));

    @Test
    void deletingPlaceRemovesOnlyItsCoachLinksBeforeDeletingRecordAndPhoto() {
        Place place = Place.builder().id(7L).photoUrl("places/7/photo.jpg").build();
        Place otherPlace = Place.builder().id(8L).build();
        Coach coach =
                Coach.builder()
                        .id(9L)
                        .workPlaces(new ArrayList<>(List.of(place, otherPlace)))
                        .build();
        place.setCoaches(List.of(coach));
        when(places.findById(7L)).thenReturn(Optional.of(place));
        placeService.deleteById(7L);
        assertEquals(List.of(otherPlace), coach.getWorkPlaces());
        var order = inOrder(places, storage);
        order.verify(places).findById(7L);
        order.verify(places).flush();
        order.verify(places).delete(place);
        order.verify(places).flush();
        order.verify(storage).deleteFile("places/7/photo.jpg");
    }

    @Test
    void updatingPlaceCanClearCoordinatesAndSubdistrictWithoutLosingPhoto() {
        Place place = Place.builder().id(7L).photoUrl("original.jpg").coordinates("1, 2").build();
        when(places.findById(7L)).thenReturn(Optional.of(place));
        PlaceDto request = new PlaceDto();
        request.setName("Новое имя");
        request.setCoordinates("");
        request.setPhotoUrl("untrusted.jpg");
        placeService.update(7L, request);
        assertEquals("Новое имя", place.getName());
        assertEquals("original.jpg", place.getPhotoUrl());
        assertNull(place.getCoordinates());
        assertNull(place.getSubDistrict());
        verifyNoInteractions(storage);
    }

    @Test
    void missingPlaceDoesNotUploadOrDeleteFiles() {
        assertThrows(NotFoundException.class, () -> placeService.deleteById(9L));
        assertThrows(
                NotFoundException.class,
                () -> placeService.uploadPhoto(9L, new MockMultipartFile("file", new byte[] {1})));
        verifyNoInteractions(storage);
    }

    @Test
    void ordinaryCoachEditPreservesDatesAndPhotoButChangedTermRenewsExpiry() {
        Coach coach = coach();
        when(coaches.findById(9L)).thenReturn(Optional.of(coach));
        when(subscribers.findById(10L)).thenReturn(coach.getSubscriber());
        CoachRequest request = coachService.getForEditing(9L);
        request.setName("Новое имя");
        coachService.updateCoachById(9L, request, null);
        assertEquals("Новое имя", coach.getName());
        assertEquals(LocalDate.of(2025, 1, 1), coach.getCreatedAt());
        assertEquals(LocalDate.of(2030, 1, 1), coach.getExpiredDateForSubscriptionToBeCoach());
        assertEquals("original.jpg", coach.getPhotoUrl());
        verifyNoInteractions(storage);
        request.setMonthsForSubscriptionToBeCoach((short) 4);
        coachService.updateCoachById(9L, request, null);
        assertEquals(LocalDate.now().plusMonths(4), coach.getExpiredDateForSubscriptionToBeCoach());
    }

    @Test
    void subscribersSeeOnlyPublishedUnexpiredCoachesWhileAdminCanManageAll() {
        Coach published = coach();
        Coach hidden = coach();
        hidden.setShowInWeb(false);
        Coach expired = coach();
        expired.setExpiredDateForSubscriptionToBeCoach(LocalDate.now().minusDays(1));
        when(coaches.findWithFilters(null, null, null, null, null))
                .thenReturn(List.of(published, hidden, expired));
        assertEquals(
                1,
                coachService.findAllCoaches(null, null, null, null, null, false).getList().size());
        assertEquals(
                3,
                coachService.findAllCoaches(null, null, null, null, null, true).getList().size());
    }

    @Test
    void usernameCreationResolvesSubscriberAndProducesEditableIdentity() {
        Subscriber subscriber = Subscriber.builder().id(10L).username("trainer").build();
        when(subscribers.findByUsername("trainer")).thenReturn(Optional.of(subscriber));
        when(coaches.save(any()))
                .thenAnswer(
                        invocation -> {
                            Coach coach = invocation.getArgument(0);
                            coach.setId(9L);
                            return coach;
                        });
        CoachRequest request = new CoachMapperDelegate() {}.coachToRequest(coach());
        request.setSubscriberId(null);
        request.setSubscriberUsername("@trainer");
        assertEquals(9L, coachService.createCoach(request, null));
        verify(coaches)
                .save(
                        argThat(
                                coach ->
                                        coach.getSubscriber() == subscriber
                                                && coach.getSportTypes()
                                                        .equals(List.of(SportType.FOOTBALL))));
    }

    private Coach coach() {
        return Coach.builder()
                .id(9L)
                .name("Тренер")
                .subscriber(Subscriber.builder().id(10L).username("trainer").build())
                .sportTypes(List.of(SportType.FOOTBALL))
                .sex(Sex.MALE)
                .age(30)
                .yearsOfExperience(5)
                .description("Описание")
                .education("Образование")
                .monthsForSubscriptionToBeCoach((short) 3)
                .showInWeb(true)
                .createdAt(LocalDate.of(2025, 1, 1))
                .expiredDateForSubscriptionToBeCoach(LocalDate.of(2030, 1, 1))
                .photoUrl("original.jpg")
                .build();
    }
}
