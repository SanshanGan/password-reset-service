package com.sanshan.passwordresetservice.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Request to execute a password reset")
data class ExecutePasswordResetRequest(
    @field:NotBlank(message = "Reset token is required")
    @field:Schema(
        description = "Reset token received from initiate endpoint",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true
    )
    val resetToken: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:Schema(
        description = "New password (minimum 8 characters)",
        example = "NewP@ssw0rd",
        required = true,
        minLength = 8
    )
    val newPassword: String
)
