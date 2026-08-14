package com.sport.service.services;

import com.sport.service.entities.Coach;
import com.sport.service.entities.TrainingProgram;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.mappers.training_program.TrainingProgramMapper;
import com.sport.service.repositories.CoachRepository;
import com.sport.service.repositories.TrainingProgramRepository;
import com.sport.service.utils.BeanUtils;
import com.sport.service.web.models.training_program.CreateTrainingProgramRequest;
import com.sport.service.web.models.training_program.ListTrainingProgramResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public ListTrainingProgramResponse findAll(List<String> sportTypesRequest) {
        log.info("findAll TrainingPrograms | sportTypes={}", sportTypesRequest);
        List<SportType> sportTypes = null;
        if (sportTypesRequest != null && !sportTypesRequest.isEmpty()) {
            sportTypes = sportTypesRequest.stream()
                    .map(SportType::valueOf)
                    .collect(Collectors.toList());
        }
        List<TrainingProgram> programs = trainingProgramRepository.findWithFilters(sportTypes);
        log.info("findAll TrainingPrograms | filtered sportTypes={} | found={} programs", sportTypes, programs.size());

        return trainingProgramMapper.listTrainingProgramToListTrainingProgramResponse(programs);
    }

    @Transactional
    public void create(CreateTrainingProgramRequest request) {
        List<Coach> creators = new ArrayList<>();
        for (Long id : request.getCoachesId()) {
            Coach coach = coachRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Coach with id " + id + " not found"));
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
        TrainingProgram program = trainingProgramRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Training program with id " + id + " not found"));

        List<Coach> creators = new ArrayList<>();
        for (Long coachId : request.getCoachesId()) {
            Coach coach = coachRepository.findById(coachId)
                    .orElseThrow(() -> new NotFoundException("Coach with id " + coachId + " not found"));
            creators.add(coach);
        }

        TrainingProgram updated =
                trainingProgramMapper.createTrainingProgramRequestToTrainingProgram(request, creators);

        BeanUtils.copyNonNullProperties(updated, program);
        trainingProgramRepository.save(program);
    }
}
