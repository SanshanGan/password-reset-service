package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.dto.PasswordResetExecutionResponse
import com.sanshan.passwordresetservice.dto.PasswordResetResponse
import com.sanshan.passwordresetservice.entity.PasswordResetRequest
import com.sanshan.passwordresetservice.entity.persistedId
import com.sanshan.passwordresetservice.exception.ActiveRequestExistsException
import com.sanshan.passwordresetservice.exception.InvalidEmailException
import com.sanshan.passwordresetservice.exception.InvalidTokenException
import com.sanshan.passwordresetservice.exception.TokenAlreadyUsedException
import com.sanshan.passwordresetservice.exception.UserNotFoundException
import com.sanshan.passwordresetservice.repository.PasswordResetRequestRepository
import com.sanshan.passwordresetservice.util.EmailValidator
import com.sanshan.passwordresetservice.util.TokenGenerator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class PasswordResetServiceImpl(
    private val userService: UserService,
    private val passwordResetRequestRepository: PasswordResetRequestRepository,
    private val emailValidator: EmailValidator,
    private val tokenGenerator: TokenGenerator,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder
) : PasswordResetService {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Transactional
    override fun initiatePasswordReset(email: String): PasswordResetResponse {
        logger.info("Password reset initiated for email: {}", email)

        if (!emailValidator.isValid(email)) {
            logger.warn("Invalid email format: {}", email)
            throw InvalidEmailException(email)
        }

        val user = userService.findByEmail(email)
            ?: run {
                logger.warn("User not found for email: {}", email)
                throw UserNotFoundException(email)
            }

        val currentTime = LocalDateTime.now()
        val hasActiveRequest = passwordResetRequestRepository.existsByUserAndUsedFalseAndExpiresAtAfter(
            user,
            currentTime
        )

        if (hasActiveRequest) {
            logger.warn("Active password reset request already exists for email: {}", email)
            throw ActiveRequestExistsException(email)
        }

        val rawToken = tokenGenerator.generateResetToken()
        val hashedToken = passwordEncoder.encode(rawToken)
        val expiresAt = currentTime.plusMinutes(30)

        val resetRequest = PasswordResetRequest(
            user = user,
            token = hashedToken, // Store hashed token
            expiresAt = expiresAt
        )

        passwordResetRequestRepository.save(resetRequest)

        logger.info("Reset token generated for email: {}, expires at: {}", email, expiresAt)

        // Return raw token to user (in production, this would be sent via email)
        return PasswordResetResponse(
            resetToken = rawToken,
            expiresAt = expiresAt.format(dateTimeFormatter)
        )
    }

    @Transactional
    override fun executePasswordReset(token: String, newPassword: String): PasswordResetExecutionResponse {
        logger.info("Executing password reset")

        val currentTime = LocalDateTime.now()
        
        // Fetch all active (unused and not expired) reset requests
        val activeRequests = passwordResetRequestRepository.findAllByUsedFalseAndExpiresAtAfter(currentTime)
        
        // Find the request where the provided token matches the hashed token
        val resetRequest = activeRequests.firstOrNull { request ->
            passwordEncoder.matches(token, request.token)
        } ?: run {
            logger.warn("Invalid token: token not found or expired")
            throw InvalidTokenException()
        }

        val user = resetRequest.user
        userService.updatePassword(user.persistedId, newPassword)

        resetRequest.used = true
        resetRequest.usedAt = currentTime
        passwordResetRequestRepository.save(resetRequest)

        logger.info("Password successfully reset for user: {}", user.email)

        return PasswordResetExecutionResponse(
            message = "Password successfully reset"
        )
    }
}
