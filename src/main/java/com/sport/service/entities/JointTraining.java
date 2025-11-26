package com.sport.service.entities;

import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.common.SportType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(name = "approved_by_admin")
    public Boolean approvedByAdmin;
}