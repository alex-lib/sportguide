package com.sport.service.mappers.training_program;

import com.sport.service.entities.Coach;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.entities.training_program.TrainingProgram;
import com.sport.service.mappers.string.SportTypeStringMapper;
import com.sport.service.services.impl.CoachServiceImpl;
import com.sport.service.web.models.training_program.CreateTrainingProgramRequest;
import com.sport.service.web.models.training_program.ListTrainingProgramResponse;
import com.sport.service.web.models.training_program.TrainingProgramResponse;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public abstract class TrainingProgramMapperDelegate implements TrainingProgramMapper {
    private CoachServiceImpl coachServiceImpl;

    @Autowired
    public TrainingProgramMapperDelegate(CoachServiceImpl coachServiceImpl) {
        this.coachServiceImpl = coachServiceImpl;
    }

    @Override
    public TrainingProgram createTrainingProgramRequestToTrainingProgram(String mongoId, CreateTrainingProgramRequest request) {
        List<SportType> sportTypesEnum = SportTypeStringMapper.listSportTypeStringToListSportTypeEnum(request.getSportTypes());
        List<Coach> creators = new ArrayList<>();
        for (Long id : request.getCoachesId()) {
            Coach coach = coachServiceImpl.findById(id);
            creators.add(coach);
        }

        return TrainingProgram.builder()
                .creators(creators)
                .title(request.getTitle())
                .price(request.getPrice())
                .description(request.getDescription())
                .sportTypes(sportTypesEnum)
                .programIdInMongoDB(mongoId)
                .showInWeb(true)
                .build();
    }

    @Override
    public ListTrainingProgramResponse listTrainingProgramToListTrainingProgramResponse(List<TrainingProgram> trainingPrograms) {
        List<TrainingProgramResponse> trainingProgramResponses = new ArrayList<>();

        for (TrainingProgram trainingProgram : trainingPrograms) {
            List<String> sportTypesStrings = SportTypeStringMapper.listSportTypeEnumToListSportTypeString(trainingProgram.getSportTypes());
            List<String> creators = trainingProgram.getCreators().stream().map(Coach::getName).toList();

            trainingProgramResponses.add(new TrainingProgramResponse(
                    trainingProgram.getTitle(),
                    creators,
                    trainingProgram.getPrice(),
                    trainingProgram.getDescription(),
                    sportTypesStrings));
        }
        return new ListTrainingProgramResponse(trainingProgramResponses);
    }
}