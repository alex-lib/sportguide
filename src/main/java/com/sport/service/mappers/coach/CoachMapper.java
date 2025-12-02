package com.sport.service.mappers.coach;

import com.sport.service.entities.Coach;
import com.sport.service.web.models.coach.CreateCoachRequest;
import com.sport.service.web.models.coach.ListCoachResponse;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@DecoratedWith(CoachMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE) //uses
public interface CoachMapper {

    Coach createCoachRequestToCoach(CreateCoachRequest request);

    default ListCoachResponse listCoachToListCoachResponse(List<Coach> coaches) {
        return null;
    }
}