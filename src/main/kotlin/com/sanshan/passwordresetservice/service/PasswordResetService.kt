package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.entity.PasswordResetResult

interface PasswordResetService {
    fun initiatePasswordReset(email: String): PasswordResetResult
    fun executePasswordReset(token: String, newPassword: String)
}
