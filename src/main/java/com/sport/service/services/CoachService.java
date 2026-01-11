package com.sport.service.services;

import com.sport.service.constants.Constants;
import com.sport.service.entities.Coach;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.TrainingProgram;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.mappers.coach.CoachMapper;
import com.sport.service.repositories.CoachRepository;
import com.sport.service.specifications.CoachSpecification;
import com.sport.service.utils.BeanUtils;
import com.sport.service.web.models.coach.CoachFilter;
import com.sport.service.web.models.coach.CoachRequest;
import com.sport.service.web.models.coach.ListCoachResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoachService {
    private final CoachRepository coachRepository;

    private final SubscriberService subscriberService;
    private final PlaceService placeService;
    private final TrainingProgramService trainingProgramService;

    private final CoachMapper coachMapper;

    public ListCoachResponse findAllCoaches(CoachFilter filter) {
        return coachMapper.listCoachToListCoachResponse(
                coachRepository.findAll(CoachSpecification.withFilter(filter)));
    }

    @Transactional
    public void createCoach(CoachRequest request, MultipartFile photo) {
        byte[] photoBytes = null;
        try {
            photoBytes = photo.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Subscriber subscriber = subscriberService.findById(request.getSubscriberId());
        List<TrainingProgram> trainingPrograms = new ArrayList<>();
        List<String> titles = request.getTrainingProgramsTitles();
        if (titles != null) {
            for (String title : titles) {
                trainingProgramService.findByTitle(title)
                        .ifPresent(trainingPrograms::add);
            }
        }

        List<Place> workPlaces = new ArrayList<>();
        for (String workPlaceName : request.getWorkPlacesNames()) {
            workPlaces.add(placeService.findByName(workPlaceName));
        }

        coachRepository.save(coachMapper.coachRequestToCoach(request, photoBytes, subscriber, trainingPrograms, workPlaces));
    }

    @Transactional
    @Scheduled(cron = Constants.CRON_TURN_OFF_DISPLAY_IN_WEB_COACH, zone = Constants.TIME_ZONE)
    public void turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach() {
        coachRepository.turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach(LocalDate.now());
    }

    public Coach findCoachById(Long id) {
        return coachRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coach with id " + id + " was not found"));
    }

    @Transactional
    public void deleteCoachById(Long id) {
        coachRepository.deleteById(id);
    }

    @Transactional
    public void updateCoachById(Long id, CoachRequest request, MultipartFile photo) {
        byte[] photoBytes = null;
        try {
            photoBytes = photo.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Coach coach = findCoachById(id);
        Subscriber subscriber = subscriberService.findById(request.getSubscriberId());
        List<TrainingProgram> trainingPrograms = new ArrayList<>();
        List<String> titles = request.getTrainingProgramsTitles();
        if (titles != null) {
            for (String title : titles) {
                trainingProgramService.findByTitle(title)
                        .ifPresent(trainingPrograms::add);
            }
        }

        List<Place> workPlaces = new ArrayList<>();
        for (String workPlaceName : request.getWorkPlacesNames()) {
            workPlaces.add(placeService.findByName(workPlaceName));
        }
        Coach updatedCoach = coachMapper.coachRequestToCoach(request, photoBytes, subscriber, trainingPrograms, workPlaces);
        BeanUtils.copyNonNullProperties(updatedCoach, coach);
        coachRepository.save(coach);
    }
}