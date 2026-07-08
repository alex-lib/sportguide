package com.sport.service.services;

import com.sport.service.entities.JointTraining;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.joint_training.ApprovalStatus;
import com.sport.service.entities.enums.subscriber.RoleType;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.mappers.joint_training.JointTrainingMapper;
import com.sport.service.processors.JointTrainingProcessor;
import com.sport.service.repositories.JointTrainingRepository;
import com.sport.service.specifications.JointTrainingSpecification;
import com.sport.service.utils.BeanUtils;
import com.sport.service.web.models.joint_training.CreateJointTrainingRequest;
import com.sport.service.web.models.joint_training.JointTrainingFilter;
import com.sport.service.web.models.joint_training.ListJointTrainingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.sport.service.entities.enums.joint_training.ApprovalStatus.PENDING;

@Service
@RequiredArgsConstructor
public class JointTrainingService {
    private final JointTrainingRepository jointTrainingRepository;

    private final JointTrainingMapper jointTrainingMapper;

    private final SubscriberService subscriberService;

    private final JointTrainingProcessor processor;

    public ListJointTrainingResponse findAllJointTrainings(JointTrainingFilter filter) {
        return jointTrainingMapper.jointTrainingListToListJointTrainingResponse(
                jointTrainingRepository.findAll(JointTrainingSpecification.withFilter(filter)));
    }

    @Transactional
    public void createJointTraining(CreateJointTrainingRequest request, Long userId) {
        Subscriber subscriber = subscriberService.findById(userId);

        JointTraining jointTraining =
                jointTrainingMapper.createJointTrainingRequestToJointTraining(request, subscriber);

        jointTraining.setApprovalStatus(PENDING);

        jointTraining = jointTrainingRepository.save(jointTraining);

        jointTraining.setSubscriber(subscriber);

        processor.processRequestToApproveJointTraining(jointTraining);
    }

    @Transactional
    public void updateJointTrainingById(CreateJointTrainingRequest request, Long id, Long userId) {
        Subscriber subscriber = subscriberService.findById(userId);

        JointTraining jointTraining =
                jointTrainingRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("JointTraining with id " + id + " was not found"));

        if (!jointTraining.getSubscriber().getId().equals(userId)
                && subscriber.getRole() != RoleType.ADMIN) {
            throw new RuntimeException("You don't have permission to update this joint training");
        }

        JointTraining updatedJointTraining =
                jointTrainingMapper.createJointTrainingRequestToJointTraining(request, subscriber);

        BeanUtils.copyNonNullProperties(updatedJointTraining, jointTraining);

        jointTraining.setApprovalStatus(ApprovalStatus.PENDING);
        jointTrainingRepository.save(jointTraining);

        processor.processRequestToApproveJointTraining(jointTraining);
    }

    @Transactional
    public void deleteJointTrainingById(Long id, Long userId) {
        Subscriber subscriber = subscriberService.findById(userId);

        JointTraining jointTraining =
                jointTrainingRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("JointTraining with id " + id + " was not found"));

        if (!jointTraining.getSubscriber().getId().equals(userId)
                && subscriber.getRole() != RoleType.ADMIN) {
            throw new RuntimeException("You don't have permission to delete this joint training");
        }

        jointTrainingRepository.deleteById(id);
    }

    @Transactional
    public void approveJointTraining(Long id) {
        JointTraining jt = jointTrainingRepository.findById(id)
                .orElseThrow();

        if (jt.getApprovalStatus() != ApprovalStatus.PENDING)
            return;

        jt.setApprovalStatus(ApprovalStatus.APPROVED);
        jt.setApprovedAt(LocalDateTime.now());
        jointTrainingRepository.save(jt);
        processor.notifyUserApproved(jt);
    }

    @Transactional
    public void rejectJointTraining(Long id, String reason) {
        JointTraining jt = jointTrainingRepository.findById(id)
                .orElseThrow();

        if (jt.getApprovalStatus() != ApprovalStatus.PENDING)
            return;

        jt.setApprovalStatus(ApprovalStatus.REJECTED);
        jt.setRejectionReason(reason);
        jointTrainingRepository.save(jt);
        processor.notifyUserRejected(jt, reason);
    }
}