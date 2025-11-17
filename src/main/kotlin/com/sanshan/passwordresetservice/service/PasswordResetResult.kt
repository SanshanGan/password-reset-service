package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.entity.PasswordResetRequest

data class PasswordResetResult(
    val resetRequest: PasswordResetRequest,
    val rawToken: String
)
