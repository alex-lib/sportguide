package com.sport.service.mappers.training_program;

import com.sport.service.entities.training_program.TrainingProgram;
import com.sport.service.web.models.training_program.CreateTrainingProgramRequest;
import com.sport.service.web.models.training_program.ListTrainingProgramResponse;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@DecoratedWith(TrainingProgramMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainingProgramMapper {

    TrainingProgram createTrainingProgramRequestToTrainingProgram(String mongoId, CreateTrainingProgramRequest request);

    default ListTrainingProgramResponse listTrainingProgramToListTrainingProgramResponse(List<TrainingProgram> trainingPrograms) {
        return null;
    }
}