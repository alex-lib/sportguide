package com.sport.service.mappers.coach;

import com.sport.service.dto.CoachDto;
import com.sport.service.entities.Coach;
import com.sport.service.web.models.coach.CoachResponse;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@DecoratedWith(CoachMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE) //uses
public interface CoachMapper {

    Coach coachDtoToCoach(CoachDto dto);

    List<CoachResponse> ListCoachToListCoachResponseList(List<Coach> coaches);
}