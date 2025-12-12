package com.sport.service.specifications;

import com.sport.service.entities.enums.common.SportType;
import com.sport.service.entities.training_program.TrainingProgram;
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
        List<SportType> sportTypes = SportTypeStringMapper.listSportTypeStringToListSportTypeEnum(sportTypesStrings);
        return (root, query, cb) -> {
            if (sportTypes == null) {
                return cb.conjunction();
            }
//            TODO change logic to find training program if at least there is one coincidence
            return cb.equal(root.get("sportTypes"), sportTypes);
        };
    }
}