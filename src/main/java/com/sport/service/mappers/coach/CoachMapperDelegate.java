package com.sport.service.mappers.coach;

import com.sport.service.entities.Coach;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.coach.Sex;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.entities.TrainingProgram;
import com.sport.service.mappers.string.SexStringMapper;
import com.sport.service.mappers.string.SportTypeStringMapper;
import com.sport.service.web.models.coach.CoachResponse;
import com.sport.service.web.models.coach.CoachRequest;
import com.sport.service.web.models.coach.ListCoachResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class CoachMapperDelegate implements CoachMapper {

    @Override
    public Coach coachRequestToCoach(CoachRequest request, String photoUrl, Subscriber subscriber, List<TrainingProgram> trainingPrograms, List<Place> workPlaces) {

        LocalDate dateForSubscriptionToBeCoach = LocalDate.now().plusMonths(request.getMonthsForSubscriptionToBeCoach());
        Sex sex = SexStringMapper.sexStringToSexEnum(request.getSex());

        List<SportType> sportTypesEnum = SportTypeStringMapper.listSportTypeStringToListSportTypeEnum(request.getSportTypes());

            return Coach.builder()
                    .subscriber(subscriber)
                    .name(request.getName())
                    .sportTypes(sportTypesEnum)
                    .description(request.getDescription())
                    .age(request.getAge())
                    .sex(sex)
                    .yearsOfExperience(request.getYearsOfExperience())
                    .education(request.getEducation())
                    .phoneNumber(request.getPhoneNumber())
                    .workPlaces(workPlaces)
                    .photoUrl(photoUrl)
                    .monthsForSubscriptionToBeCoach(request.getMonthsForSubscriptionToBeCoach())
                    .createdAt(LocalDate.now())
                    .expiredDateForSubscriptionToBeCoach(dateForSubscriptionToBeCoach)
                    .showInWeb(request.getShowInWeb())
                    .trainingPrograms(trainingPrograms)
                    .build();
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

            coachResponses.add(CoachResponse.builder()
                    .name(coach.getName())
                    .sportTypes(sportTypesStrings)
                    .description(coach.getDescription())
                    .age(coach.getAge())
                    .sex(sexString)
                    .yearsOfExperience(coach.getYearsOfExperience())
                    .education(coach.getEducation())
                    .phoneNumber(coach.getPhoneNumber())
                    .workPlacesNames(workPlacesName)
                    .photoUrl(coach.getPhotoUrl())
                    .telegramUsername(link)
                    .trainingProgramsNames(trainingProgramsTitles)
                    .build());
        }
        return new ListCoachResponse(coachResponses);
    }
}