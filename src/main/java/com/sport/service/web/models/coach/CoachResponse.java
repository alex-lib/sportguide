package com.sport.service.web.models.coach;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoachResponse {
    private String name;
    private List<String> sportTypes;
    private String description;
    private Integer age;
    private String sex;
    private Integer yearsOfExperience;
    private String education;
    private String phoneNumber;
    private List<String> workPlacesNames;
    private byte[] photo;
    private String telegramUsername;
    private List<String> trainingProgramsNames;
}