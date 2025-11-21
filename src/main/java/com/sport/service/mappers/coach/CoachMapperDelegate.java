package com.sport.service.mappers.coach;

import com.sport.service.dto.CoachDto;
import com.sport.service.entities.Coach;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.training_program.TrainingProgram;
import com.sport.service.mappers.SexStringMapper;
import com.sport.service.mappers.SportTypeStringMapper;
import com.sport.service.services.PlaceService;
import com.sport.service.services.SubscriberService;
import com.sport.service.services.impl.TrainingProgramServiceImpl;
import com.sport.service.web.models.coach.CoachResponse;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class CoachMapperDelegate implements CoachMapper {
    private final SubscriberService subscriberService;
    private final PlaceService placeService;
    private final TrainingProgramServiceImpl trainingProgramService;

    @Override
    public Coach coachDtoToCoach(CoachDto dto) {
        Subscriber subscriber = subscriberService.findById(dto.getSubscriberId());
        LocalDate dateForSubscriptionToBeCoach = LocalDate.now().plusMonths(dto.getMonthsForSubscriptionToBeCoach());

        List<TrainingProgram> trainingPrograms = new ArrayList<>();
        for (String trainingProgramTitle : dto.getTrainingProgramsTitles()) {
            trainingPrograms.add(trainingProgramService.findByTitle(trainingProgramTitle).orElse(null));
        }

        List<Place> workPlaces = new ArrayList<>();
        for (String workPlaceName : dto.getWorkPlacesNames()) {
            workPlaces.add(placeService.findByName(workPlaceName));
        }

        Coach coach = Coach.builder()
                .subscriber(subscriber)
                .name(dto.getName())
                .sportTypes(dto.getSportTypes())
                .description(dto.getDescription())
                .age(dto.getAge())
                .sex(dto.getSex())
                .yearsOfExperience(dto.getYearsOfExperience())
                .education(dto.getEducation())
                .phoneNumber(dto.getPhoneNumber())
                .workPlaces(workPlaces)
                .photo(dto.getPhoto())
                .monthsForSubscriptionToBeCoach(dto.getMonthsForSubscriptionToBeCoach())
                .createdAt(LocalDate.now())
                .expiredDateForSubscriptionToBeCoach(dateForSubscriptionToBeCoach)
                .showInWeb(dto.getShowInWeb())
                .trainingPrograms(trainingPrograms)
                .build();
        return coach;
    }

    @Override
    public List<CoachResponse> ListCoachToListCoachResponseList(List<Coach> coaches) {
        List<CoachResponse> coachResponses = new ArrayList<>();

        for (Coach coach : coaches) {

            List<String> sportTypesStrings = SportTypeStringMapper.listSportTypeEnumToListSportTypeString(coach.getSportTypes());
            String sexString = SexStringMapper.sexEnumToSexString(coach.getSex());
            List<String> workPlacesName = coach.getWorkPlaces().stream().map(Place::getName).toList();
            String telegramUsername = coach.getSubscriber().getUsername();
            List<String> trainingProgramsTitles = coach.getTrainingPrograms().stream().map(TrainingProgram::getTitle).toList();

            coachResponses.add(new CoachResponse(
                    coach.getName(),
                    sportTypesStrings,
                    coach.getDescription(),
                    coach.getAge(),
                    sexString,
                    coach.getYearsOfExperience(),
                    coach.getEducation(),
                    coach.getPhoneNumber(),
                    workPlacesName,
                    coach.getPhoto(),
                    telegramUsername,
                    trainingProgramsTitles));
        }
        return coachResponses;
    }
}