package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.entity.PasswordResetRequest

interface PasswordResetService {
    fun initiatePasswordReset(email: String): PasswordResetResult
    fun executePasswordReset(token: String, newPassword: String)
}
