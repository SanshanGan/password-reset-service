package com.sanshan.passwordresetservice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ExecutePasswordResetRequest(
    @field:NotBlank(message = "Reset token is required")
    val resetToken: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String
)
