package com.sport.service.services.impl

import com.sport.service.entities.JointTraining
import com.sport.service.entities.Subscriber
import com.sport.service.entities.enums.subscriber.RoleType
import com.sport.service.mappers.joint_training.JointTrainingMapper
import com.sport.service.repositories.JointTrainingRepository
import com.sport.service.services.SubscriberService
import com.sport.service.specifications.JointTrainingSpecification
import com.sport.service.utils.BeanUtils
import com.sport.service.web.models.joint_training.CreateJointTrainingRequest
import com.sport.service.web.models.joint_training.JointTrainingFilter
import com.sport.service.web.models.joint_training.ListJointTrainingResponse
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class JointTrainingServiceImpl(
    private var jointTrainingRepository: JointTrainingRepository,
    private var jointTrainingMapper: JointTrainingMapper,
    private var subscriberService: SubscriberService
) {

    fun findAll(filter: JointTrainingFilter): ListJointTrainingResponse {
        return jointTrainingMapper.jointTrainingListToListJointTrainingResponse(
            jointTrainingRepository.findAll(
                JointTrainingSpecification.withFilter(filter)
            )
        )
    }

    @Transactional
    fun create(request: CreateJointTrainingRequest, jwt: JwtAuthenticationToken) {
        val subscriberId: Long = jwt.token.subject.toLong()
        val subscriber = subscriberService.findById(subscriberId)

        val jointTraining: JointTraining = jointTrainingMapper.createJointTrainingRequestToJointTraining(request)
        jointTraining.subscriber = subscriber

        //TODO: create admin's approval through telegram before saving to DB new joint training
        jointTraining.approvedByAdmin = false
        jointTrainingRepository.save(jointTraining)
    }

    @Transactional
    fun update(request: CreateJointTrainingRequest, id: Long, jwt: JwtAuthenticationToken) {
        val subscriberId: Long = jwt.token.subject.toLong()
        val subscriber: Subscriber = subscriberService.findById(subscriberId)
        val jointTraining: JointTraining = jointTrainingRepository.findById(id).orElse(null)

        if (jointTraining.subscriber.id != subscriberId && subscriber.role != RoleType.ADMIN) {
            throw RuntimeException("You don't have permission to update this joint training")
        }

        val updatedJointTraining: JointTraining = jointTrainingMapper.createJointTrainingRequestToJointTraining(request)
        BeanUtils.copyNonNullProperties(updatedJointTraining, jointTraining)

        //TODO: create admin's approval through telegram before saving to DB updates of existed joint training
        jointTrainingRepository.save(jointTraining)
    }

    @Transactional
    fun delete(id: Long, jwt: JwtAuthenticationToken) {
        val subscriberId: Long = jwt.token.subject.toLong()
        val subscriber: Subscriber = subscriberService.findById(subscriberId)
        val jointTraining: JointTraining = jointTrainingRepository.findById(id).orElse(null)

        if (jointTraining.subscriber.id != subscriberId && subscriber.role != RoleType.ADMIN) {
            throw RuntimeException("You don't have permission to update this joint training")
        }

        jointTrainingRepository.deleteById(id)
    }
}