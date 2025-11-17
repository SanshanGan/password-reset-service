package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.entity.PasswordResetRequest

/**
 * Result wrapper for password reset initiation.
 * Contains both the persisted reset request and the raw (unhashed) token
 * that needs to be returned to the client.
 */
data class PasswordResetResult(
    val resetRequest: PasswordResetRequest,
    val rawToken: String
)
