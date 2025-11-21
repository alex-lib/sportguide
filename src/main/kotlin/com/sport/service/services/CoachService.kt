package com.sport.service.services

import com.sport.service.dto.CoachDto
import com.sport.service.entities.Coach
import com.sport.service.web.models.coach.CoachFilter

interface CoachService {

    fun findAll(filter: CoachFilter): List<Coach>

    fun create(dto: CoachDto)

    fun turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach()
}