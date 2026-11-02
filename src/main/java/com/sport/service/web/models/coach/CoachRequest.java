package com.sport.service.web.models.coach;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sport.service.annotations.PhoneNumberValid;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CoachRequest {

    private Long subscriberId;
    private String subscriberUsername;
    private String photoUrl;

    @NotBlank(message = "Name must be pointed")
    @Size(max = 255)
    private String name;

    @NotEmpty(message = "Sport types must be pointed")
    private List<String> sportTypes;

    @NotBlank(message = "Description must be pointed")
    private String description;

    @Min(value = 18, message = "Age mustn't be under 18")
    @NotNull
    @Max(120)
    private Integer age;

    @NotBlank(message = "Sex must be pointed")
    private String sex;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer yearsOfExperience;

    @NotBlank private String education;

    @PhoneNumberValid private String phoneNumber;

    private List<String> workPlacesNames = List.of();

    @NotNull
    @Min(1)
    @Max(120)
    private Short monthsForSubscriptionToBeCoach;

    @NotNull private Boolean showInWeb;

    private List<String> trainingProgramsTitles = List.of();

    @AssertTrue(message = "Укажите Telegram пользователя тренера")
    public boolean isSubscriberValid() {
        return subscriberId != null && subscriberId > 0
                || subscriberUsername != null && subscriberUsername.matches("@?[A-Za-z0-9_]{5,32}");
    }

    @AssertTrue(message = "Выберите пол и виды спорта из списка")
    public boolean isClassificationValid() {
        return com.sport.service.mappers.string.SexStringMapper.sexStringToSexEnum(
                                sex == null ? "" : sex)
                        != null
                && sportTypes != null
                && !sportTypes.isEmpty()
                && sportTypes.stream().allMatch(java.util.Objects::nonNull)
                && com.sport.service.mappers.string.SportTypeStringMapper
                                .listSportTypeStringToListSportTypeEnum(sportTypes)
                                .size()
                        == sportTypes.size();
    }
}
