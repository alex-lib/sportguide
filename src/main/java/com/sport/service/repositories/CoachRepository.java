package com.sport.service.repositories;

import com.sport.service.entities.enums.coach.Sex;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.entities.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {

    @Modifying
    @Query("""
            UPDATE Coach c
            SET c.showInWeb = false
            WHERE c.expiredDateForSubscriptionToBeCoach > :date""")
    void turnOffToShowInWebByExpiredDateForSubscriptionToBeCoach(LocalDate date);

    @Query(nativeQuery = true, value = """
            SELECT DISTINCT c.* FROM coaches c
            WHERE (:sportTypes IS NULL OR c.sport_types && :sportTypes)
              AND (:sex IS NULL OR c.sex = :sex)
              AND (:age IS NULL OR c.age = :age)
              AND (:yearsOfExperience IS NULL OR c.years_of_experience = :yearsOfExperience)
            """)
    List<Coach> findWithFilters(
            @Param("sportTypes") List<SportType> sportTypes,
            @Param("sex") Sex sex,
            @Param("age") Integer age,
            @Param("yearsOfExperience") Integer yearsOfExperience
    );
}
