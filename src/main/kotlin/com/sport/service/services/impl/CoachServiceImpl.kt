package com.sport.service.services.impl

import com.sport.service.dto.CoachDto
import com.sport.service.entities.Coach
import com.sport.service.mappers.coach.CoachMapper
import com.sport.service.repositories.CoachRepository
import com.sport.service.services.CoachService
import com.sport.service.specifications.CoachSpecification
import com.sport.service.web.models.coach.CoachFilter
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class CoachServiceImpl(
    private var coachRepository: CoachRepository,
    private var coachMapper: CoachMapper
) : CoachService {

    override fun findAll(filter: CoachFilter): List<Coach> {
        return coachRepository.findAll(CoachSpecification.withFilter(filter))
    }

    override fun create(dto: CoachDto) {
        coachRepository.save(coachMapper.coachDtoToCoach(dto))
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Moscow")
    override fun turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach() {
        coachRepository.turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach(LocalDate.now())
    }
}