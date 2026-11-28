package com.sport.service.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sport.service.configurations.MinioService;
import com.sport.service.entities.Coach;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.mappers.coach.CoachMapper;
import com.sport.service.repositories.CoachRepository;
import com.sport.service.web.models.coach.CoachResponse;
import com.sport.service.web.models.coach.ListCoachResponse;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

class CoachProfileServiceTest {
    private final CoachRepository repository = mock(CoachRepository.class);
    private final CoachMapper mapper = mock(CoachMapper.class);
    private final CoachService service =
            new CoachService(
                    repository,
                    mock(SubscriberService.class),
                    mock(PlaceService.class),
                    mock(TrainingProgramService.class),
                    mock(MinioService.class),
                    mapper,
                    mock(ApplicationEventPublisher.class));

    @Test
    void subscriberCannotReadHiddenExpiredOrMissingProfiles() {
        for (Coach coach :
                List.of(
                        Coach.builder().showInWeb(false).build(),
                        Coach.builder().showInWeb(null).build(),
                        Coach.builder()
                                .showInWeb(true)
                                .expiredDateForSubscriptionToBeCoach(LocalDate.now().minusDays(1))
                                .build())) {
            when(repository.findById(1L)).thenReturn(Optional.of(coach));
            assertThrows(NotFoundException.class, () -> service.getProfile(1L, false));
        }
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getProfile(1L, false));
        verifyNoInteractions(mapper);
    }

    @Test
    void publishedProfileIsAvailableThroughItsExpiryDateAndWithoutExpiry() {
        Coach coach =
                Coach.builder()
                        .showInWeb(true)
                        .expiredDateForSubscriptionToBeCoach(LocalDate.now())
                        .build();
        when(repository.findById(1L)).thenReturn(Optional.of(coach));
        CoachResponse response =
                CoachResponse.builder().id(1L).name("Тренер").education("Образование").build();
        when(mapper.listCoachToListCoachResponse(List.of(coach)))
                .thenReturn(new ListCoachResponse(List.of(response)));
        assertSame(response, service.getProfile(1L, false));
        coach.setExpiredDateForSubscriptionToBeCoach(null);
        assertSame(response, service.getProfile(1L, false));
    }

    @Test
    void adminCanPreviewUnpublishedProfile() {
        Coach coach =
                Coach.builder()
                        .showInWeb(false)
                        .expiredDateForSubscriptionToBeCoach(LocalDate.now().minusDays(1))
                        .build();
        when(repository.findById(1L)).thenReturn(Optional.of(coach));
        CoachResponse response = CoachResponse.builder().id(1L).build();
        when(mapper.listCoachToListCoachResponse(List.of(coach)))
                .thenReturn(new ListCoachResponse(List.of(response)));
        assertSame(response, service.getProfile(1L, true));
    }
}
