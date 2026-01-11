package com.sport.service.mappers.training_program;

import com.sport.service.entities.Coach;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.entities.TrainingProgram;
import com.sport.service.mappers.string.SportTypeStringMapper;
import com.sport.service.web.models.training_program.CreateTrainingProgramRequest;
import com.sport.service.web.models.training_program.ListTrainingProgramResponse;
import com.sport.service.web.models.training_program.TrainingProgramResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class TrainingProgramMapperDelegate implements TrainingProgramMapper {

    @Override
    public TrainingProgram createTrainingProgramRequestToTrainingProgram(CreateTrainingProgramRequest request, List<Coach> creators) {
        List<SportType> sportTypesEnum = SportTypeStringMapper.listSportTypeStringToListSportTypeEnum(request.getSportTypes());

        return TrainingProgram.builder()
                .creators(creators)
                .title(request.getTitle())
                .price(request.getPrice())
                .description(request.getDescription())
                .sportTypes(sportTypesEnum)
                .showInWeb(true)
                .createdAt(LocalDate.now())
                .haveVideoMaterials(request.getHaveVideoMaterials())
                .contactToBuy(request.getContactToBuy())
                .build();
    }

    @Override
    public ListTrainingProgramResponse listTrainingProgramToListTrainingProgramResponse(List<TrainingProgram> trainingPrograms) {
        List<TrainingProgramResponse> trainingProgramResponses = new ArrayList<>();

        for (TrainingProgram trainingProgram : trainingPrograms) {
            List<String> sportTypesStrings = SportTypeStringMapper.listSportTypeEnumToListSportTypeString(trainingProgram.getSportTypes());
            List<String> creators = trainingProgram.getCreators().stream().map(Coach::getName).toList();
            String contactToBuy = String.format("[@%s](%s)", trainingProgram.getContactToBuy(), "https://t.me/" + trainingProgram.getContactToBuy());

            trainingProgramResponses.add(
                    TrainingProgramResponse.builder()
                            .title(trainingProgram.getTitle())
                            .creators(creators)
                            .price(trainingProgram.getPrice())
                            .description(trainingProgram.getDescription())
                            .sportTypes(sportTypesStrings)
                            .contactToBuy(contactToBuy)
                            .build());
        }
        return new ListTrainingProgramResponse(trainingProgramResponses);
    }
}