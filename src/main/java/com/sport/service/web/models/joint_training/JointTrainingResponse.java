package com.sport.service.web.models.joint_training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JointTrainingResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private String sportType;
    private String placeName;
    private String district;
    private String address;
    private String creatorName;
    private String phoneNumber;
    private String linkToChatWithCreator;
}