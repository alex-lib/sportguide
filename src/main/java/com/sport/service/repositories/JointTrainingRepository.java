package com.sport.service.repositories;

import com.sport.service.entities.JointTraining;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.common.SportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JointTrainingRepository extends JpaRepository<JointTraining, Long> {

    @Query("""
            SELECT jt FROM JointTraining jt
            WHERE jt.approvalStatus = ApprovalStatus.APPROVED
              AND (:district IS NULL OR jt.district = :district)
              AND (:date IS NULL OR jt.date = :date)
              AND (:sportTypes IS NULL OR jt.sportType IN :sportTypes)
            """)
    List<JointTraining> findWithFilters(
            @Param("district") District district,
            @Param("date") LocalDate date,
            @Param("sportTypes") List<SportType> sportTypes
    );
}
