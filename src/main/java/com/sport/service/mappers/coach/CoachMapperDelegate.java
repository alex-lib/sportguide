package com.sport.service.mappers.coach;

import com.sport.service.entities.Coach;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.coach.Sex;
import com.sport.service.entities.training_program.TrainingProgram;
import com.sport.service.mappers.string.SexStringMapper;
import com.sport.service.mappers.string.SportTypeStringMapper;
import com.sport.service.services.PlaceService;
import com.sport.service.services.SubscriberService;
import com.sport.service.services.impl.TrainingProgramServiceImpl;
import com.sport.service.web.models.coach.CoachResponse;
import com.sport.service.web.models.coach.CreateCoachRequest;
import com.sport.service.web.models.coach.ListCoachResponse;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public abstract class CoachMapperDelegate implements CoachMapper {
    private SubscriberService subscriberService;
    private PlaceService placeService;
    private TrainingProgramServiceImpl trainingProgramService;

    @Autowired
    public CoachMapperDelegate(SubscriberService subscriberService, PlaceService placeService, TrainingProgramServiceImpl trainingProgramService) {
        this.subscriberService = subscriberService;
        this.placeService = placeService;
        this.trainingProgramService = trainingProgramService;
    }

    @Override
    public Coach createCoachRequestToCoach(CreateCoachRequest request) {
        Subscriber subscriber = subscriberService.findById(request.getSubscriberId());
        LocalDate dateForSubscriptionToBeCoach = LocalDate.now().plusMonths(request.getMonthsForSubscriptionToBeCoach());
        Sex sex = SexStringMapper.sexStringToSexEnum(request.getSex());

        List<TrainingProgram> trainingPrograms = new ArrayList<>();
        for (String trainingProgramTitle : request.getTrainingProgramsTitles()) {
            trainingPrograms.add(trainingProgramService.findByTitle(trainingProgramTitle).orElse(null));
        }

        List<Place> workPlaces = new ArrayList<>();
        for (String workPlaceName : request.getWorkPlacesNames()) {
            workPlaces.add(placeService.findByName(workPlaceName));
        }

        Coach coach = Coach.builder()
                .subscriber(subscriber)
                .name(request.getName())
                .sportTypes(request.getSportTypes())
                .description(request.getDescription())
                .age(request.getAge())
                .sex(sex)
                .yearsOfExperience(request.getYearsOfExperience())
                .education(request.getEducation())
                .phoneNumber(request.getPhoneNumber())
                .workPlaces(workPlaces)
                .photo(request.getPhoto())
                .monthsForSubscriptionToBeCoach(request.getMonthsForSubscriptionToBeCoach())
                .createdAt(LocalDate.now())
                .expiredDateForSubscriptionToBeCoach(dateForSubscriptionToBeCoach)
                .showInWeb(request.getShowInWeb())
                .trainingPrograms(trainingPrograms)
                .build();
        return coach;
    }

    @Override
    public ListCoachResponse listCoachToListCoachResponse(List<Coach> coaches) {
        List<CoachResponse> coachResponses = new ArrayList<>();

        for (Coach coach : coaches) {
            List<String> sportTypesStrings = SportTypeStringMapper.listSportTypeEnumToListSportTypeString(coach.getSportTypes());
            String sexString = SexStringMapper.sexEnumToSexString(coach.getSex());
            List<String> workPlacesName = coach.getWorkPlaces().stream().map(Place::getName).toList();
            List<String> trainingProgramsTitles = coach.getTrainingPrograms().stream().map(TrainingProgram::getTitle).toList();
            String telegramUsername = coach.getSubscriber().getUsername();
            String link = String.format("[@%s](%s)", telegramUsername, "https://t.me/" + telegramUsername);

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
                    link,
                    trainingProgramsTitles));
        }
        return new ListCoachResponse(coachResponses);
    }
}