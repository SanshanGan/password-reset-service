package com.sanshan.passwordresetservice.controller

import com.sanshan.passwordresetservice.dto.ExecutePasswordResetRequest
import com.sanshan.passwordresetservice.dto.InitiatePasswordResetRequest
import com.sanshan.passwordresetservice.dto.PasswordResetResponse
import com.sanshan.passwordresetservice.service.PasswordResetService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/password-reset")
@Tag(name = "Password Reset", description = "Endpoints for managing password reset requests")
class PasswordResetController(
    private val passwordResetService: PasswordResetService
) {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Operation(
        summary = "Initiate password reset",
        description = "Generates a time-limited, single-use reset token for a user. The token expires after 30 minutes and can only be used once."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Reset token generated successfully",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PasswordResetResponse::class),
                    examples = [ExampleObject(
                        value = """{"resetToken": "550e8400-e29b-41d4-a716-446655440000", "expiresAt": "2025-11-17T16:30:00"}"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid email format",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """{"errorCode": "INVALID_EMAIL", "message": "Invalid email format: invalid-email"}"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """{"errorCode": "USER_NOT_FOUND", "message": "User not found: user@example.com"}"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Active reset request already exists",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """{"errorCode": "ACTIVE_REQUEST_EXISTS", "message": "Active password reset request already exists for: user@example.com"}"""
                    )]
                )]
            )
        ]
    )
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

    @Operation(
        summary = "Execute password reset",
        description = "Completes the password reset process using a valid reset token. The token must be unused and not expired."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Password successfully reset"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid, expired, or already used token / Invalid password format",
                content = [Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            name = "Invalid Token",
                            value = """{"errorCode": "INVALID_TOKEN", "message": "Invalid or expired reset token"}"""
                        ),
                        ExampleObject(
                            name = "Validation Error",
                            value = """{"errorCode": "VALIDATION_ERROR", "message": "newPassword: size must be between 8 and 100"}"""
                        )
                    ]
                )]
            )
        ]
    )
    @PostMapping("/execute")
    fun executePasswordReset(
        @Valid @RequestBody request: ExecutePasswordResetRequest
    ): ResponseEntity<Void> {
        passwordResetService.executePasswordReset(
            request.resetToken,
            request.newPassword
        )
        return ResponseEntity.noContent().build()
    }
}
