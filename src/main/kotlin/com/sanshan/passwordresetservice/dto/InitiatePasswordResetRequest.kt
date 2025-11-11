package com.sanshan.passwordresetservice.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class InitiatePasswordResetRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String
)
