package com.sanshan.passwordresetservice.service

interface PasswordResetService {
    fun initiatePasswordReset(email: String): PasswordResetResult
    fun executePasswordReset(token: String, newPassword: String)
}
