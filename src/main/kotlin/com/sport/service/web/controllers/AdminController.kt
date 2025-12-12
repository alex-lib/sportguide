//package com.sport.service.web.controllers
//
//import com.sport.service.services.impl.CoachServiceImpl
//import com.sport.service.services.impl.TrainingProgramServiceImpl
//import com.sport.service.web.models.coach.CreateCoachRequest
//import com.sport.service.web.models.training_program.CreateTrainingProgramRequest
//import jakarta.validation.Valid
//import org.springframework.http.HttpStatus
//import org.springframework.http.MediaType
//import org.springframework.security.access.prepost.PreAuthorize
//import org.springframework.web.bind.annotation.*
//import org.springframework.web.multipart.MultipartFile
//
//@RestController
//@RequestMapping("/admin")
//class AdminController(
//    private var coachService: CoachServiceImpl,
//    private var trainingProgramService: TrainingProgramServiceImpl
//) {
//
//    @GetMapping
//    fun test(): String {
//        return "Test, look here!"
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @ResponseStatus(HttpStatus.CREATED)
//    @PostMapping("/coaches")
//    fun createCoach(@Valid @RequestBody request: CreateCoachRequest) {
//        coachService.create(request)
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/coaches/{id}")
//    fun deleteCoach(@RequestParam("id") id: Long) {
//        coachService.delete(id)
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PutMapping("/coaches/{id}")
//    fun updateCoach(@RequestParam("id") id: Long, @Valid @RequestBody request: CreateCoachRequest) {
//        coachService.update(id, request)
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping(
//        "/program-trainings",
//        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
//    )
//    @ResponseStatus(HttpStatus.CREATED)
//    fun createTrainingProgram(
//        @Valid @RequestPart("data") request: CreateTrainingProgramRequest,
//        @RequestPart("file") file: MultipartFile
//    ) {
//        trainingProgramService.create(request, file)
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/program-trainings/{id}")
//    fun createTrainingProgram(@RequestParam("id") id: Long) {
//        trainingProgramService.delete(id)
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PutMapping(
//        "/program-trainings/{id}",
//        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
//    )
//    @ResponseStatus(HttpStatus.CREATED)
//    fun updateTrainingProgram(
//        @RequestParam("id") id: Long,
//        @Valid @RequestPart("data") request: CreateTrainingProgramRequest,
//        @RequestPart("file") file: MultipartFile?
//    ) {
//        trainingProgramService.update(request, file, id)
//    }
//}