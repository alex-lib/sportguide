//package com.sport.service.web.controllers
//
//import com.sport.service.services.impl.CoachServiceImpl
//import com.sport.service.web.models.coach.CoachFilter
//import com.sport.service.web.models.coach.ListCoachResponse
//import org.springframework.security.access.prepost.PreAuthorize
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RequestMapping("/coaches")
//@RestController
//class CoachController(
//    private val coachService: CoachServiceImpl
//) {
//
//    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
//    @GetMapping
//    fun findAll(filter: CoachFilter): ListCoachResponse {
//        return coachService.findAll(filter)
//    }
//}