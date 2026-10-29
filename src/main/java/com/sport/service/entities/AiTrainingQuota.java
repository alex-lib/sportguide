package com.sport.service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ai_training_quota")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiTrainingQuota {
    @Id
    @Column(name = "subscriber_id", nullable = false)
    private Long subscriberId;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;
}
