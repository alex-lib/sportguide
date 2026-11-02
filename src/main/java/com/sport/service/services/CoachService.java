package com.sport.service.services;

import com.sport.service.configurations.MinioService;
import com.sport.service.constants.Constants;
import com.sport.service.entities.Coach;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.TrainingProgram;
import com.sport.service.entities.enums.coach.Sex;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.mappers.coach.CoachMapper;
import com.sport.service.repositories.CoachRepository;
import com.sport.service.store.notifications.Notification;
import com.sport.service.utils.BeanUtils;
import com.sport.service.web.models.coach.CoachRequest;
import com.sport.service.web.models.coach.ListCoachResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

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
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public ListCoachResponse findAllCoaches(
            List<String> sportTypesRequest,
            Integer age,
            String sex,
            Integer yearsOfExperience,
            String search) {
        return findAllCoaches(sportTypesRequest, age, sex, yearsOfExperience, search, false);
    }

    @Transactional(readOnly = true)
    public ListCoachResponse findAllCoaches(
            List<String> sportTypesRequest,
            Integer age,
            String sex,
            Integer yearsOfExperience,
            String search,
            boolean includeHidden) {
        List<SportType> sportTypes = null;
        if (sportTypesRequest != null && !sportTypesRequest.isEmpty()) {
            sportTypes =
                    sportTypesRequest.stream().map(SportType::valueOf).collect(Collectors.toList());
        }
        Sex sexEnum = sex != null ? Sex.valueOf(sex) : null;
        var result =
                coachRepository.findWithFilters(
                        sportTypes, sexEnum, age, yearsOfExperience, search);
        if (!includeHidden)
            result =
                    result.stream()
                            .filter(coach -> Boolean.TRUE.equals(coach.getShowInWeb()))
                            .filter(
                                    coach ->
                                            coach.getExpiredDateForSubscriptionToBeCoach() == null
                                                    || !coach.getExpiredDateForSubscriptionToBeCoach()
                                                            .isBefore(LocalDate.now()))
                            .toList();
        return coachMapper.listCoachToListCoachResponse(result);
    }

    @Transactional
    public Long createCoach(CoachRequest request, MultipartFile photo) {
        Coach coach = saveNewCoach(request);

        if (photo != null && !photo.isEmpty()) {
            String objectName = "coaches/" + coach.getId() + "/" + photo.getOriginalFilename();
            minioService.uploadFile(objectName, photo);
            coach.setPhotoUrl(minioService.getFileUrl(objectName));
            coachRepository.save(coach);
            log.info(
                    "Photo uploaded to MinIO for new coach '{}' (id={}): {}",
                    request.getName(),
                    coach.getId(),
                    objectName);
        }
        return coach.getId();
    }

    @Transactional
    public void createCoachFromBot(CoachRequest request, byte[] photo) {
        Coach coach = saveNewCoach(request);
        String objectName = "coaches/" + coach.getId() + "/photo.jpg";
        minioService.uploadFileBytes(objectName, photo, "image/jpeg");
        coach.setPhotoUrl(minioService.getFileUrl(objectName));
        coachRepository.save(coach);
    }

    @Transactional(readOnly = true)
    public CoachRequest getForEditing(Long id) {
        return coachMapper.coachToRequest(findCoachById(id));
    }

    private Subscriber resolveSubscriber(CoachRequest request) {
        if (request.getSubscriberId() != null) {
            Subscriber subscriber = subscriberService.findById(request.getSubscriberId());
            if (subscriber == null)
                throw new IllegalArgumentException("Пользователь тренера не найден");
            return subscriber;
        }
        String username = request.getSubscriberUsername();
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Укажите Telegram пользователя тренера");
        return subscriberService
                .findByUsername(username.replaceFirst("^@", ""))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Пользователь не найден. Тренер должен сначала открыть бота"
                                            + " SportGuide."));
    }

    private Coach saveNewCoach(CoachRequest request) {
        Subscriber subscriber = resolveSubscriber(request);
        List<TrainingProgram> trainingPrograms = new ArrayList<>();
        List<String> titles = request.getTrainingProgramsTitles();
        if (titles != null) {
            for (String title : titles) {
                trainingProgramService.findByTitle(title).ifPresent(trainingPrograms::add);
            }
        }

        List<Place> workPlaces = new ArrayList<>();
        for (String workPlaceName :
                request.getWorkPlacesNames() == null
                        ? List.<String>of()
                        : request.getWorkPlacesNames()) {
            Place place = placeService.findByName(workPlaceName);
            if (place == null)
                throw new IllegalArgumentException("Место не найдено: " + workPlaceName);
            workPlaces.add(place);
        }

        Coach coach =
                coachMapper.coachRequestToCoach(
                        request, null, subscriber, trainingPrograms, workPlaces);
        Coach saved = coachRepository.save(coach);
        for (TrainingProgram program : trainingPrograms) program.getCreators().add(saved);
        if (Boolean.TRUE.equals(saved.getShowInWeb())) {
            events.publishEvent(
                    Notification.builder()
                            .type(Notification.NotificationType.COACH)
                            .message(
                                    "🏅 Новый тренер: <b>"
                                            + HtmlUtils.htmlEscape(saved.getName())
                                            + "</b>\n"
                                            + "Посмотрите анкету в разделе «Тренеры» приложения.")
                            .build());
        }
        return saved;
    }

    @Transactional
    @Scheduled(cron = Constants.CRON_TURN_OFF_DISPLAY_IN_WEB_COACH, zone = Constants.TIME_ZONE)
    public void turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach() {
        coachRepository.turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach(LocalDate.now());
    }

    public Coach findCoachById(Long id) {
        return coachRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Coach with id " + id + " was not found"));
    }

    @Transactional
    public void deleteCoachById(Long id) {
        deleteCoach(findCoachById(id));
    }

    @Transactional
    public boolean deleteCoachByUsername(String username) {
        Coach coach =
                subscriberService
                        .findByUsername(username)
                        .flatMap(
                                subscriber ->
                                        coachRepository.findBySubscriberId(subscriber.getId()))
                        .orElse(null);
        if (coach == null) return false;
        deleteCoach(coach);
        return true;
    }

    private void deleteCoach(Coach coach) {
        for (TrainingProgram program : coach.getTrainingPrograms()) {
            program.getCreators().removeIf(creator -> coach.getId().equals(creator.getId()));
        }
        coachRepository.delete(coach);
        coachRepository.flush();
        if (coach.getPhotoUrl() != null) {
            String objectName = coach.getPhotoUrl().replaceFirst("^https?://", "");
            String storagePrefix = minioService.getFileUrl("");
            if (objectName.startsWith(storagePrefix)) {
                objectName = objectName.substring(storagePrefix.length());
            }
            minioService.deleteFile(objectName);
        }
    }

    @Transactional
    public void updateCoachById(Long id, CoachRequest request, MultipartFile photo) {
        Coach coach = findCoachById(id);
        Subscriber subscriber = resolveSubscriber(request);
        List<TrainingProgram> trainingPrograms = new ArrayList<>();
        List<String> titles = request.getTrainingProgramsTitles();
        if (titles != null) {
            for (String title : titles) {
                trainingProgramService.findByTitle(title).ifPresent(trainingPrograms::add);
            }
        }

        List<Place> workPlaces = new ArrayList<>();
        for (String workPlaceName :
                request.getWorkPlacesNames() == null
                        ? List.<String>of()
                        : request.getWorkPlacesNames()) {
            Place place = placeService.findByName(workPlaceName);
            if (place == null)
                throw new IllegalArgumentException("Место не найдено: " + workPlaceName);
            workPlaces.add(place);
        }

        String photoUrl = coach.getPhotoUrl();
        if (photo != null && !photo.isEmpty()) {
            String objectName = "coaches/" + id + "/" + java.util.UUID.randomUUID();
            minioService.uploadFile(objectName, photo);
            photoUrl = minioService.getFileUrl(objectName);
            log.info(
                    "Photo updated in MinIO for coach '{}' (id={}): {}",
                    coach.getName(),
                    id,
                    objectName);
        }

        Coach updatedCoach =
                coachMapper.coachRequestToCoach(
                        request, photoUrl, subscriber, trainingPrograms, workPlaces);
        updatedCoach.setCreatedAt(coach.getCreatedAt());
        if (java.util.Objects.equals(
                coach.getMonthsForSubscriptionToBeCoach(),
                request.getMonthsForSubscriptionToBeCoach())) {
            updatedCoach.setExpiredDateForSubscriptionToBeCoach(
                    coach.getExpiredDateForSubscriptionToBeCoach());
        }
        // TrainingProgram owns the relationship; update both sides, including removals.
        for (TrainingProgram program : coach.getTrainingPrograms()) {
            if (!trainingPrograms.contains(program))
                program.getCreators().removeIf(creator -> id.equals(creator.getId()));
        }
        for (TrainingProgram program : trainingPrograms) {
            if (program.getCreators().stream().noneMatch(creator -> id.equals(creator.getId())))
                program.getCreators().add(coach);
        }
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
