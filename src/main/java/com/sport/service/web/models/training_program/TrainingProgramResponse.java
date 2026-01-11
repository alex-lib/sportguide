package com.sport.service.web.models.training_program;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgramResponse {
    private String title;
    private List<String> creators;
    private BigDecimal price;
    private String description;
    private List<String> sportTypes;
    private String contactToBuy;
}