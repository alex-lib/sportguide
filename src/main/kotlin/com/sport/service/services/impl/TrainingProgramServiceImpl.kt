package com.sport.service.services.impl

import com.sport.service.entities.training_program.TrainingProgram
//import com.sport.service.entities.training_program.TrainingProgramDocument
import com.sport.service.mappers.training_program.TrainingProgramMapper
import com.sport.service.repositories.TrainingProgramRepository
import com.sport.service.specifications.ProgramTrainingSpecification
import com.sport.service.utils.BeanUtils
//import com.sport.service.web.models.payment.PaymentLinkResponse
import com.sport.service.web.models.training_program.CreateTrainingProgramRequest
import com.sport.service.web.models.training_program.ListTrainingProgramResponse
import com.sport.service.web.models.training_program.TrainingProgramFilter
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class TrainingProgramServiceImpl(
    private var trainingProgramRepository: TrainingProgramRepository,
    private var trainingProgramMapper: TrainingProgramMapper,
    private var paymentServiceImpl: PaymentServiceImpl,
//    private var trainingProgramDocumentService: TrainingProgramDocumentServiceImpl
) {

    fun findByTitle(title: String): Optional<TrainingProgram> {
        return trainingProgramRepository.findByTitle(title)
    }

    fun findById(id: Long): TrainingProgram {
        return trainingProgramRepository.findById(id).orElse(null)
    }

    fun findAll(filter: TrainingProgramFilter): ListTrainingProgramResponse {
        val trainingPrograms: List<TrainingProgram> =
            trainingProgramRepository.findAll(ProgramTrainingSpecification.withFilter(filter))
        return trainingProgramMapper.listTrainingProgramToListTrainingProgramResponse(trainingPrograms)
    }

    fun buyTrainingProgram(id: Long,){ //jwt: JwtAuthenticationToken): PaymentLinkResponse {
        //val subscriberId: Long = jwt.token.subject.toLong()
        val trainingProgram: TrainingProgram = findById(id)
        //paymentServiceImpl.createPayment(subscriberId, trainingProgram)
//        val trainingProgramDocument: TrainingProgramDocument =
//            trainingProgramDocumentService.findById(trainingProgram.programIdInMongoDB)
        //TODO: generate link and set a payment provider
        //TODO: and if client paid so increment payment count
//        return PaymentLinkResponse("This is a link to pay")
    }

    @Transactional
    fun create(request: CreateTrainingProgramRequest, file: MultipartFile) {
//        val mongoId: String = trainingProgramDocumentService.save(file)
//        val trainingProgram: TrainingProgram =
//            trainingProgramMapper.createTrainingProgramRequestToTrainingProgram(mongoId, request)
//        trainingProgramRepository.save(trainingProgram)
    }

    @Transactional
    fun delete(id: Long) {
        val trainingProgram: TrainingProgram = findById(id)
//        trainingProgramDocumentService.delete(trainingProgram.programIdInMongoDB)
        trainingProgramRepository.deleteById(id)
    }

    @Transactional
    fun update(request: CreateTrainingProgramRequest, file: MultipartFile?, id: Long) {
        val trainingProgram: TrainingProgram = findById(id)
        val updatedTrainingProgram: TrainingProgram =
            trainingProgramMapper.createTrainingProgramRequestToTrainingProgram(
                trainingProgram.programIdInMongoDB,
                request
            )
//        trainingProgramDocumentService.update(trainingProgram.programIdInMongoDB, file)
        BeanUtils.copyNonNullProperties(updatedTrainingProgram, trainingProgram)
        trainingProgramRepository.save(trainingProgram)
    }
}