package com.sport.service.services;

import com.sport.service.configurations.MinioService;
import com.sport.service.entities.Coach;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.TrainingProgram;
import com.sport.service.mappers.coach.CoachMapper;
import com.sport.service.repositories.CoachRepository;
import com.sport.service.web.models.coach.CoachRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;

class CoachServiceTest {
    private final CoachRepository repository = mock(CoachRepository.class);
    private final SubscriberService subscribers = mock(SubscriberService.class);
    private final PlaceService places = mock(PlaceService.class);
    private final TrainingProgramService programs = mock(TrainingProgramService.class);
    private final MinioService minio = mock(MinioService.class);
    private final CoachMapper mapper = mock(CoachMapper.class);
    private final CoachService service = new CoachService(repository, subscribers, places, programs, minio, mapper);
    private final CoachRequest request = new CoachRequest();
    private final Coach coach = new Coach();

    @BeforeEach
    void setUp() {
        request.setSubscriberId(99L);
        request.setWorkPlacesNames(List.of("Зал"));
        Subscriber subscriber = new Subscriber();
        Place place = new Place();
        when(subscribers.findById(99L)).thenReturn(subscriber);
        when(places.findByName("Зал")).thenReturn(place);
        when(mapper.coachRequestToCoach(request, null, subscriber, List.of(), List.of(place))).thenReturn(coach);
        when(repository.save(any(Coach.class))).thenAnswer(call -> {
            Coach saved = call.getArgument(0);
            saved.setId(42L);
            return saved;
        });
    }

    @Test
    void botPhotoUsesGeneratedCoachIdAndPersistsUrl() {
        byte[] photo = {1, 2};
        when(minio.getFileUrl("coaches/42/photo.jpg")).thenReturn("storage/coaches/42/photo.jpg");
        service.createCoachFromBot(request, photo);
        verify(minio).uploadFileBytes("coaches/42/photo.jpg", photo, "image/jpeg");
        assertEquals("storage/coaches/42/photo.jpg", coach.getPhotoUrl());
        verify(repository, times(2)).save(coach);
    }

    @Test
    void existingMultipartCreationStillUploadsPhoto() {
        MockMultipartFile photo = new MockMultipartFile("photo", "avatar.jpg", "image/jpeg", new byte[]{1});
        when(minio.getFileUrl("coaches/42/avatar.jpg")).thenReturn("storage/coaches/42/avatar.jpg");
        service.createCoach(request, photo);
        verify(minio).uploadFile("coaches/42/avatar.jpg", photo);
        assertEquals("storage/coaches/42/avatar.jpg", coach.getPhotoUrl());
    }

    @Test
    void uploadFailurePropagatesForTransactionRollback() {
        byte[] photo = {1};
        doThrow(new IllegalStateException("Storage unavailable"))
                .when(minio).uploadFileBytes("coaches/42/photo.jpg", photo, "image/jpeg");
        assertThrows(IllegalStateException.class, () -> service.createCoachFromBot(request, photo));
        assertNull(coach.getPhotoUrl());
        verify(repository).save(coach);
    }

    @ParameterizedTest
    @ValueSource(strings = {"coaches/42/photo.jpg", "storage/bucket/coaches/42/photo.jpg",
            "https://storage/bucket/coaches/42/photo.jpg"})
    void deleteByUsernameRemovesCoachAndPhotoButPreservesSubscriberAndPrograms(String photoUrl) {
        Subscriber subscriber = Subscriber.builder().id(99L).build();
        coach.setId(42L);
        coach.setPhotoUrl(photoUrl);
        when(minio.getFileUrl("")).thenReturn("storage/bucket/");
        Coach otherCoach = Coach.builder().id(43L).build();
        TrainingProgram program = TrainingProgram.builder()
                .creators(new ArrayList<>(List.of(coach, otherCoach))).build();
        coach.setTrainingPrograms(List.of(program));
        when(subscribers.findByUsername("@coach_name")).thenReturn(Optional.of(subscriber));
        when(repository.findBySubscriberId(99L)).thenReturn(Optional.of(coach));

        assertTrue(service.deleteCoachByUsername("@coach_name"));

        assertEquals(List.of(otherCoach), program.getCreators());
        var order = inOrder(repository, minio);
        order.verify(repository).delete(coach);
        order.verify(repository).flush();
        order.verify(minio).deleteFile("coaches/42/photo.jpg");
        verify(subscribers).findByUsername("@coach_name");
        verifyNoMoreInteractions(subscribers);
        verifyNoInteractions(programs, places);
    }

    @Test
    void unknownUsernameDoesNotDeleteAnything() {
        assertFalse(service.deleteCoachByUsername("@unknown_coach"));
        verifyNoInteractions(repository, minio);
    }

    @Test
    void subscriberWithoutCoachProfileDoesNotDeleteAnything() {
        when(subscribers.findByUsername("@subscriber"))
                .thenReturn(Optional.of(Subscriber.builder().id(99L).build()));
        assertFalse(service.deleteCoachByUsername("@subscriber"));
        verify(repository, never()).delete(any(Coach.class));
        verifyNoInteractions(minio);
    }

    @Test
    void failedDatabaseDeletionKeepsPhoto() {
        coach.setId(42L);
        coach.setPhotoUrl("coaches/42/photo.jpg");
        when(subscribers.findByUsername("@coach_name"))
                .thenReturn(Optional.of(Subscriber.builder().id(99L).build()));
        when(repository.findBySubscriberId(99L)).thenReturn(Optional.of(coach));
        doThrow(new IllegalStateException("Database unavailable")).when(repository).flush();
        assertThrows(IllegalStateException.class, () -> service.deleteCoachByUsername("@coach_name"));
        verifyNoInteractions(minio);
    }
}
