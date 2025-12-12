package com.sport.service.validation.joint_training

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [PhoneNumberValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PhoneNumberValid(
    val message: String = "Invalid phone number format. Example: +7XXXXXXXXXX",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)