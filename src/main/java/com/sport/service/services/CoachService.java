package com.sport.service.services;

import com.sport.service.constants.Constants;
import com.sport.service.configurations.MinioService;
import com.sport.service.entities.Coach;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.TrainingProgram;
import com.sport.service.entities.enums.coach.Sex;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.mappers.coach.CoachMapper;
import com.sport.service.repositories.CoachRepository;
import com.sport.service.utils.BeanUtils;
import com.sport.service.web.models.coach.CoachRequest;
import com.sport.service.web.models.coach.ListCoachResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoachService {
    private final CoachRepository coachRepository;
    private final SubscriberService subscriberService;
    private final PlaceService placeService;
    private final TrainingProgramService trainingProgramService;
    private final MinioService minioService;

    private final CoachMapper coachMapper;

      public ListCoachResponse findAllCoaches(List<String> sportTypesRequest, Integer age, String sex, Integer yearsOfExperience, String search) {
          log.info("findAll Coaches | sportTypes={}, age={}, sex={}, yearsOfExperience={}, search={}",
                  sportTypesRequest, age, sex, yearsOfExperience, search);
          List<SportType> sportTypes = null;
          if (sportTypesRequest != null && !sportTypesRequest.isEmpty()) {
              sportTypes = sportTypesRequest.stream()
                      .map(SportType::valueOf)
                      .collect(Collectors.toList());
          }
          Sex sexEnum = sex != null ? Sex.valueOf(sex) : null;
          var result = coachRepository.findWithFilters(
                  sportTypes,
                  sexEnum,
                  age,
                  yearsOfExperience,
                  search);
          log.info("findAll Coaches | filtered sportTypes={}, sex={}, age={}, yearsOfExperience={}, search={} | found={} coaches",
                  sportTypes, sexEnum, age, yearsOfExperience, search, result.size());
          return coachMapper.listCoachToListCoachResponse(result);
      }

    @Transactional
    public void createCoach(CoachRequest request, MultipartFile photo) {
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

        Coach coach = coachMapper.coachRequestToCoach(request, null, subscriber, trainingPrograms, workPlaces);
        coachRepository.save(coach);

        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            String objectName = "coaches/" + coach.getId() + "/" + photo.getOriginalFilename();
            minioService.uploadFile(objectName, photo);
            photoUrl = minioService.getFileUrl(objectName);
            coach.setPhotoUrl(photoUrl);
            coachRepository.save(coach);
            log.info("Photo uploaded to MinIO for new coach '{}' (id={}): {}", request.getName(), coach.getId(), objectName);
        }
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

        String photoUrl = coach.getPhotoUrl();
        if (photo != null && !photo.isEmpty()) {
            if (photoUrl != null) {
                minioService.deleteFile(photoUrl);
            }
            String objectName = "coaches/" + coach.getName() + "/" + photo.getOriginalFilename();
            minioService.uploadFile(objectName, photo);
            photoUrl = minioService.getFileUrl(objectName);
            log.info("Photo updated in MinIO for coach '{}' (id={}): {}", coach.getName(), id, objectName);
        }

        Coach updatedCoach = coachMapper.coachRequestToCoach(request, photoUrl, subscriber, trainingPrograms, workPlaces);
        BeanUtils.copyNonNullProperties(updatedCoach, coach);
        coachRepository.save(coach);
    }

    @Transactional
    public String uploadPhoto(Long id, MultipartFile file) {
        Coach coach = findCoachById(id);
        String objectName = "coaches/" + id + "/" + file.getOriginalFilename();
        minioService.uploadFile(objectName, file);
        String url = minioService.getFileUrl(objectName);
        coach.setPhotoUrl(url);
        coachRepository.save(coach);
        log.info("Photo uploaded for coach '{}' (id={}): {}", coach.getName(), id, objectName);
        return url;
    }

    @Transactional
    public void deletePhoto(Long id) {
        Coach coach = findCoachById(id);
        String photoUrl = coach.getPhotoUrl();
        if (photoUrl != null) {
            minioService.deleteFile(photoUrl);
            coach.setPhotoUrl(null);
            log.info("Photo deleted for coach '{}': {}", id, photoUrl);
        }
        coachRepository.save(coach);
    }

    public byte[] getPhoto(String photoUrl) {
        if (photoUrl == null || photoUrl.isEmpty()) {
            log.warn("No photoUrl provided");
            return new byte[0];
        }
        try {
            byte[] photo = minioService.getFile(photoUrl);
            if (photo == null || photo.length == 0) {
                log.warn("No photo found in MinIO for URL: {}", photoUrl);
                return new byte[0];
            }
            return photo;
        } catch (Exception e) {
            log.error("Failed to get photo for URL {}: {}", photoUrl, e.getMessage());
            return new byte[0];
        }
    }
}
