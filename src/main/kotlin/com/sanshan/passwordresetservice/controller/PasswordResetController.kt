package com.sanshan.passwordresetservice.controller

import com.sanshan.passwordresetservice.dto.ExecutePasswordResetRequest
import com.sanshan.passwordresetservice.dto.InitiatePasswordResetRequest
import com.sanshan.passwordresetservice.dto.PasswordResetExecutionResponse
import com.sanshan.passwordresetservice.dto.PasswordResetResponse
import com.sanshan.passwordresetservice.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/password-reset")
class PasswordResetController(
    private val passwordResetService: PasswordResetService
) {

    @PostMapping("/initiate")
    fun initiatePasswordReset(
        @Valid @RequestBody request: InitiatePasswordResetRequest
    ): ResponseEntity<PasswordResetResponse> {
        val response = passwordResetService.initiatePasswordReset(request.email)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/execute")
    fun executePasswordReset(
        @Valid @RequestBody request: ExecutePasswordResetRequest
    ): ResponseEntity<PasswordResetExecutionResponse> {
        val response = passwordResetService.executePasswordReset(
            request.resetToken,
            request.newPassword
        )
        return ResponseEntity.ok(response)
    }
}
