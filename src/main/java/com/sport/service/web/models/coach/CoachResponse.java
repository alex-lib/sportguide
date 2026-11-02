package com.sport.service.web.models.coach;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoachResponse {
    private Long id;
    private String name;
    private List<String> sportTypes;
    private String description;
    private Integer age;
    private String sex;
    private Integer yearsOfExperience;
    private String education;
    private String phoneNumber;
    private List<String> workPlacesNames;
    private String photoUrl;
    private String telegramUsername;
    private List<String> trainingProgramsNames;
}
