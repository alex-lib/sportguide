package com.sport.service.web.models.joint_training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListJointTrainingResponse {
    private List<JointTrainingResponse> list;
}