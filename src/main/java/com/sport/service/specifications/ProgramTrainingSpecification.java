package com.sport.service.specifications;

import com.sport.service.entities.enums.common.SportType;
import com.sport.service.entities.TrainingProgram;
import com.sport.service.mappers.string.SportTypeStringMapper;
import com.sport.service.web.models.training_program.TrainingProgramFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ProgramTrainingSpecification {

    static Specification<TrainingProgram> withFilter(TrainingProgramFilter filter) {
        return Specification.where(byTrainingProgramTypes(filter.getSportTypes()));
    }

    static Specification<TrainingProgram> byTrainingProgramTypes(List<String> sportTypesStrings) {
        if (sportTypesStrings == null || sportTypesStrings.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        List<SportType> sportTypes =
                SportTypeStringMapper.listSportTypeStringToListSportTypeEnum(sportTypesStrings);

        return (root, query, cb) -> {
            query.distinct(true);
            return root.join("sportTypes").in(sportTypes);
        };
    }
}