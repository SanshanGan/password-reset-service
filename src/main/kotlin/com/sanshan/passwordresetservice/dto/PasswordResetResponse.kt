package com.sanshan.passwordresetservice.dto

data class PasswordResetResponse(
    val resetToken: String,
    val expiresAt: String
)
