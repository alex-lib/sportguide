package com.sport.service.validation.joint_training

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class PhoneNumberValidator : ConstraintValidator<PhoneNumberValid, String> {
    private val regex = Regex("^\\+7\\d{10}$")

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) return true
        return regex.matches(value)
    }
}