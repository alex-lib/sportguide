package com.sport.service.web.models.training_program;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainingProgramRequest {
    @NotEmpty(message = "Coaches' ids must be pointed")
    private List<Long> coachesId;
    @NotBlank(message = "Title must be pointed")
    private String title;
    private BigDecimal price;
    private String description;
    @NotEmpty(message = "Sport types must be pointed")
    private List<String> sportTypes;
    private Boolean showInWeb;
    private Boolean haveVideoMaterials;
    private String contactToBuy;
}