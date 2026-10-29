package com.sport.service.repositories;

import com.sport.service.entities.AiTrainingQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AiTrainingQuotaRepository extends JpaRepository<AiTrainingQuota, Long> {
    @Modifying
    @Query(value = """
            INSERT INTO app_schema.ai_training_quota (subscriber_id, available_at) VALUES (:subscriberId, :availableAt)
            ON CONFLICT (subscriber_id) DO UPDATE SET available_at = EXCLUDED.available_at
            WHERE ai_training_quota.available_at <= :now
            """, nativeQuery = true)
    int reserve(@Param("subscriberId") Long subscriberId, @Param("availableAt") Instant availableAt, @Param("now") Instant now);

    @Modifying
    @Query("""
            DELETE FROM AiTrainingQuota quota
            WHERE quota.subscriberId = :subscriberId AND quota.availableAt = :reservation
            """)
    int release(@Param("subscriberId") Long subscriberId, @Param("reservation") Instant reservation);
}
