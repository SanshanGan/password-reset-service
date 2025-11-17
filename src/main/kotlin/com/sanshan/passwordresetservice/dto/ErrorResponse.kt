package com.sanshan.passwordresetservice.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Error response")
data class ErrorResponse(
    @field:Schema(
        description = "Error code",
        example = "INVALID_EMAIL"
    )
    val error: String,
    
    @field:Schema(
        description = "Human-readable error message",
        example = "Invalid email format: invalid-email"
    )
    val message: String,
    
    @field:Schema(
        description = "Timestamp when the error occurred",
        example = "2025-11-17T15:30:00"
    )
    val timestamp: String = LocalDateTime.now().toString()
)
