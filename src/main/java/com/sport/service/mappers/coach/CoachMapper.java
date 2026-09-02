package com.sport.service.mappers.coach;

import com.sport.service.entities.Coach;
import com.sport.service.entities.Place;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.TrainingProgram;
import com.sport.service.web.models.coach.CoachRequest;
import com.sport.service.web.models.coach.ListCoachResponse;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@DecoratedWith(CoachMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE) //uses
public interface CoachMapper {

    Coach coachRequestToCoach(CoachRequest request, String photoUrl, Subscriber subscriber, List<TrainingProgram> trainingPrograms, List<Place> workPlaces);

    default ListCoachResponse listCoachToListCoachResponse(List<Coach> coaches) {
        return null;
    }
}