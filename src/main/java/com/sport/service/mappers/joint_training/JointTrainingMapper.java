package com.sport.service.mappers.joint_training;

import com.sport.service.entities.JointTraining;
import com.sport.service.entities.Subscriber;
import com.sport.service.web.models.joint_training.CreateJointTrainingRequest;
import com.sport.service.web.models.joint_training.ListJointTrainingResponse;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@DecoratedWith(JointTrainingMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JointTrainingMapper {

    default ListJointTrainingResponse jointTrainingListToListJointTrainingResponse(List<JointTraining> jointTraining) {
        return null;
    }

    JointTraining createJointTrainingRequestToJointTraining(CreateJointTrainingRequest request, Subscriber subscriber);
}