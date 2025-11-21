package com.sport.service.dto;

import com.sport.service.entities.enums.coach.Sex;
import com.sport.service.entities.enums.common.SportType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoachDto {
    private Long subscriberId;
    private String name;
    private List<SportType> sportTypes;
    private String description;
    private Integer age;
    private Sex sex;
    private Integer yearsOfExperience;
    private String education;
    private String phoneNumber;
    private List<String> workPlacesNames;
    private byte[] photo;
    private Short monthsForSubscriptionToBeCoach;
    private Boolean showInWeb;
    private List<String> trainingProgramsTitles;
}