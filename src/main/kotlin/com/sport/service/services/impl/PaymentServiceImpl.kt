package com.sport.service.services.impl

import com.sport.service.entities.Payment
import com.sport.service.entities.enums.payment.PaymentStatus
import com.sport.service.entities.training_program.TrainingProgram
import com.sport.service.repositories.PaymentRepository
import com.sport.service.services.SubscriberService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentServiceImpl(
    private var paymentRepository: PaymentRepository,
    private var subscriberService: SubscriberService
) {

    @Transactional
    fun createPayment(subscriberId: Long, trainingProgram: TrainingProgram) {
        if (trainingProgram == null) throw RuntimeException("Training program not found")
        val payment: Payment = Payment(
            PaymentStatus.WAITING_FOR_PAYMENT,
            subscriberService.findById(subscriberId),
            trainingProgram
        )
        paymentRepository.save(payment)
    }
}