package com.sanshan.passwordresetservice.entity

data class PasswordResetResult(
    val resetRequest: PasswordResetRequest,
    val rawToken: String
)