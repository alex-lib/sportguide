package com.sport.service.entities;

import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.entities.enums.joint_training.ApprovalStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "joint_trainings")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class JointTraining {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    public Subscriber subscriber;

    @Column(name = "description")
    private String description;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time", columnDefinition = "TIME")
    private LocalTime time;

    @Column(name = "sport_type")
    @Enumerated(EnumType.STRING)
    private SportType sportType;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "district")
    @Enumerated(EnumType.STRING)
    private District district;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(name = "approval_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "rejected_reason")
    private String rejectionReason;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}