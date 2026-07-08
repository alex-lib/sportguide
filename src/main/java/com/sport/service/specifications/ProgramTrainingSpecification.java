package com.sport.service.specifications;

import com.sport.service.entities.TrainingProgram;
import com.sport.service.web.models.training_program.TrainingProgramFilter;
import org.springframework.data.jpa.domain.Specification;

public interface ProgramTrainingSpecification {

    static Specification<TrainingProgram> withFilter(TrainingProgramFilter filter) {
        // sportTypes is a basic array column (List<SportType> mapped with @Enumerated,
        // not an @ElementCollection), so it cannot be filtered with a Criteria join.
        // It is filtered in memory in TrainingProgramService.findAll instead.
        return (root, query, cb) -> cb.conjunction();
    }
}