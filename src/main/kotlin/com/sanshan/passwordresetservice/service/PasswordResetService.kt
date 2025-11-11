package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.dto.PasswordResetExecutionResponse
import com.sanshan.passwordresetservice.dto.PasswordResetResponse

interface PasswordResetService {
    fun initiatePasswordReset(email: String): PasswordResetResponse
    fun executePasswordReset(token: String, newPassword: String): PasswordResetExecutionResponse
}
