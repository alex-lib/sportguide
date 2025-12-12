package com.sport.service.repositories

import com.sport.service.entities.Coach
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface CoachRepository : JpaRepository<Coach, Long>, JpaSpecificationExecutor<Coach> {

    @Modifying
    @Query(
        """
    UPDATE Coach c
    SET c.showInWeb = false
    WHERE c.expiredDateForSubscriptionToBeCoach > :targetDate"""
    )
    fun turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach(date: LocalDate)
}