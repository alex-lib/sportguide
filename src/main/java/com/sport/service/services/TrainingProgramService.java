package com.sport.service.services;

import com.sport.service.entities.Coach;
import com.sport.service.entities.TrainingProgram;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.mappers.training_program.TrainingProgramMapper;
import com.sport.service.repositories.CoachRepository;
import com.sport.service.repositories.TrainingProgramRepository;
import com.sport.service.specifications.ProgramTrainingSpecification;
import com.sport.service.utils.BeanUtils;
import com.sport.service.web.models.training_program.CreateTrainingProgramRequest;
import com.sport.service.web.models.training_program.ListTrainingProgramResponse;
import com.sport.service.web.models.training_program.TrainingProgramFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TrainingProgramService {
    private final TrainingProgramRepository trainingProgramRepository;
    private final CoachRepository coachRepository;

    private final TrainingProgramMapper trainingProgramMapper;

    public Optional<TrainingProgram> findByTitle(String title) {
        return trainingProgramRepository.findByTitle(title);
    }

    public TrainingProgram findById(Long id) {
        return trainingProgramRepository.findById(id).orElse(null);
    }

    public ListTrainingProgramResponse findAll(TrainingProgramFilter filter) {
        List<TrainingProgram> programs =
                trainingProgramRepository.findAll(
                        ProgramTrainingSpecification.withFilter(filter)
                );

        return trainingProgramMapper
                .listTrainingProgramToListTrainingProgramResponse(programs);
    }

    @Transactional
    public void create(CreateTrainingProgramRequest request) {
        List<Coach> creators = new ArrayList<>();
        for (Long id : request.getCoachesId()) {
            Coach coach = coachRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Coach with id " + id + " was not found"));
            creators.add(coach);
        }

         TrainingProgram program =
                trainingProgramMapper.createTrainingProgramRequestToTrainingProgram(request, creators);

         trainingProgramRepository.save(program);
    }

    @Transactional
    public void delete(Long id) {
        trainingProgramRepository.deleteById(id);
    }

    @Transactional
    public void update(CreateTrainingProgramRequest request, Long id) {
        TrainingProgram program = findById(id);

        List<Coach> creators = new ArrayList<>();
        for (Long coachId : request.getCoachesId()) {
            Coach coach = coachRepository.findById(coachId)
                    .orElseThrow(() -> new NotFoundException("Coach with id " + coachId + " was not found"));
            creators.add(coach);
        }

        TrainingProgram updated =
                trainingProgramMapper.createTrainingProgramRequestToTrainingProgram(request, creators);

        BeanUtils.copyNonNullProperties(updated, program);
        trainingProgramRepository.save(program);
    }
}