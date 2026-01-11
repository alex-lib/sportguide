package com.sport.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JointTrainingRejectingDto {
    private Long adminId;
    private Long jointTrainingId;
    private Long createdAt;
}