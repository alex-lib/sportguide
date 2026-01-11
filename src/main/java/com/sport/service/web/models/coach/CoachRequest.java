package com.sport.service.web.models.coach;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sport.service.annotations.PhoneNumberValid;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class CoachRequest {

    @NotNull(message = "Coach's id must be pointed")
    private Long subscriberId;

    @NotBlank(message = "Name must be pointed")
    private String name;

    @NotEmpty(message = "Sport types must be pointed")
    private List<String> sportTypes;

    @NotBlank(message = "Description must be pointed")
    private String description;

    @Min(value = 18, message = "Age mustn't be under 18")
    private Integer age;

    @NotBlank(message = "Sex must be pointed")
    private String sex;

    @NotNull
    private Integer yearsOfExperience;

    @NotBlank
    private String education;

    @PhoneNumberValid
    private String phoneNumber;

    private List<String> workPlacesNames;

    @NotNull
    private Short monthsForSubscriptionToBeCoach;

    @NotNull
    private Boolean showInWeb;

    private List<String> trainingProgramsTitles;
}