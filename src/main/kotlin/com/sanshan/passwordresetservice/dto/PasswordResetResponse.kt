package com.sanshan.passwordresetservice.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response containing reset token and expiration time")
data class PasswordResetResponse(
    @field:Schema(
        description = "Single-use reset token (valid for 30 minutes)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    val resetToken: String,
    
    @field:Schema(
        description = "Token expiration timestamp (ISO 8601 format)",
        example = "2025-11-17T16:30:00"
    )
    val expiresAt: String
)
