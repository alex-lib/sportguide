//package com.sport.service.web.controllers
//
//import com.sport.service.services.impl.TrainingProgramServiceImpl
//import com.sport.service.web.models.payment.PaymentLinkResponse
//import com.sport.service.web.models.training_program.ListTrainingProgramResponse
//import com.sport.service.web.models.training_program.TrainingProgramFilter
//import org.springframework.security.access.prepost.PreAuthorize
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/training-programs")
//class TrainingProgramController(
//    private var trainingProgramServiceImpl: TrainingProgramServiceImpl,
//) {
//
//    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
//    @GetMapping
//    fun findAll(filter: TrainingProgramFilter): ListTrainingProgramResponse {
//        return trainingProgramServiceImpl.findAll(filter)
//    }
//
//    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
//    @GetMapping("/{id}/buy")
//    fun buyTrainingProgram(@PathVariable id: Long, jwt: JwtAuthenticationToken): PaymentLinkResponse {
//        return trainingProgramServiceImpl.buyTrainingProgram(id, jwt)
//    }
//}