package com.sport.service.entities;

import com.sport.service.entities.enums.payment.PaymentStatus;
import com.sport.service.entities.training_program.TrainingProgram;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments", uniqueConstraints = {@UniqueConstraint(columnNames = {"subscriber_id", "training_program_id"})})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    public Payment(PaymentStatus paymentStatus, Subscriber subscriber, TrainingProgram trainingProgram) {
        this.paymentStatus = paymentStatus;
        this.subscriber = subscriber;
        this.trainingProgram = trainingProgram;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_program_id", nullable = false)
    private TrainingProgram trainingProgram;
}