package com.sport.service.services.impl

import com.sport.service.entities.Coach
import com.sport.service.mappers.coach.CoachMapper
import com.sport.service.repositories.CoachRepository
import com.sport.service.specifications.CoachSpecification
import com.sport.service.utils.BeanUtils

import com.sport.service.web.models.coach.CoachFilter
import com.sport.service.web.models.coach.CreateCoachRequest
import com.sport.service.web.models.coach.ListCoachResponse
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CoachServiceImpl(
    private var coachRepository: CoachRepository,
    private var coachMapper: CoachMapper
) {

    fun findAll(filter: CoachFilter): ListCoachResponse {
        return coachMapper.listCoachToListCoachResponse(coachRepository.findAll(CoachSpecification.withFilter(filter)))
    }

    @Transactional
    fun create(request: CreateCoachRequest) {
        coachRepository.save(coachMapper.createCoachRequestToCoach(request))
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Moscow")
    fun turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach() {
        coachRepository.turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach(LocalDate.now())
    }

    fun findById(id: Long): Coach {
        return coachRepository.findById(id).orElse(null)
    }

    @Transactional
    fun delete(id: Long) {
        coachRepository.deleteById(id)
    }

    @Transactional
    fun update(id: Long, request: CreateCoachRequest) {
        val coach: Coach = findById(id)
        val updatedCoach: Coach = coachMapper.createCoachRequestToCoach(request)
        BeanUtils.copyNonNullProperties(updatedCoach, coach)
        coachRepository.save(coach)
    }
}