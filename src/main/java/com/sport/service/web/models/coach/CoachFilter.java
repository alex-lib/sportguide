package com.sport.service.web.models.coach;

import lombok.Data;
import java.util.List;

@Data
public class CoachFilter {
    private List<String> sportTypes;
    private Integer age;
    private String sex;
    private Integer yearsOfExperience;
}
