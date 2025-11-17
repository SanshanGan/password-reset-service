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
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/password-reset")
class PasswordResetController(
    private val passwordResetService: PasswordResetService
) {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @PostMapping("/initiate")
    fun initiatePasswordReset(
        @Valid @RequestBody request: InitiatePasswordResetRequest
    ): ResponseEntity<PasswordResetResponse> {
        val result = passwordResetService.initiatePasswordReset(request.email)
        val response = PasswordResetResponse(
            resetToken = result.rawToken,
            expiresAt = result.resetRequest.expiresAt.format(dateTimeFormatter)
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/execute")
    fun executePasswordReset(
        @Valid @RequestBody request: ExecutePasswordResetRequest
    ): ResponseEntity<PasswordResetExecutionResponse> {
        passwordResetService.executePasswordReset(
            request.resetToken,
            request.newPassword
        )
        val response = PasswordResetExecutionResponse(
            message = "Password successfully reset"
        )
        return ResponseEntity.ok(response)
    }
}
