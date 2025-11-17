package com.sanshan.passwordresetservice.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(description = "Request to initiate a password reset")
data class InitiatePasswordResetRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @field:Schema(
        description = "User's email address",
        example = "user@example.com",
        required = true
    )
    val email: String
)
