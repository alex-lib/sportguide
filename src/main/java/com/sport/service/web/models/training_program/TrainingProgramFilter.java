package com.sport.service.web.models.training_program;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgramFilter {
    private List<String> sportTypes;
}