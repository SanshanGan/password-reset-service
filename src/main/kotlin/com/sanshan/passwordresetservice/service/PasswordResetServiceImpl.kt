package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.entity.PasswordResetRequest
import com.sanshan.passwordresetservice.entity.persistedId
import com.sanshan.passwordresetservice.exception.ActiveRequestExistsException
import com.sanshan.passwordresetservice.exception.InvalidEmailException
import com.sanshan.passwordresetservice.exception.InvalidTokenException
import com.sanshan.passwordresetservice.exception.UserNotFoundException
import com.sanshan.passwordresetservice.repository.PasswordResetRequestRepository
import com.sanshan.passwordresetservice.util.EmailValidator
import com.sanshan.passwordresetservice.util.TokenGenerator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PasswordResetServiceImpl(
    private val userService: UserService,
    private val passwordResetRequestRepository: PasswordResetRequestRepository,
    private val emailValidator: EmailValidator,
    private val tokenGenerator: TokenGenerator,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder
) : PasswordResetService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun initiatePasswordReset(email: String): PasswordResetResult {
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
            token = hashedToken,
            expiresAt = expiresAt
        )

        val savedRequest = passwordResetRequestRepository.save(resetRequest)

        logger.info("Reset token generated for email: {}, expires at: {}", email, expiresAt)

        return PasswordResetResult(
            resetRequest = savedRequest,
            rawToken = rawToken
        )
    }

    @Transactional
    override fun executePasswordReset(token: String, newPassword: String) {
        logger.info("Executing password reset")

        val currentTime = LocalDateTime.now()
        val activeRequests = passwordResetRequestRepository.findAllByUsedFalseAndExpiresAtAfter(currentTime)
        
        val resetRequest = findMatchingResetRequest(token, activeRequests, currentTime)
            ?: throw InvalidTokenException()

        val user = resetRequest.user
        userService.updatePassword(user.persistedId, newPassword)

        resetRequest.used = true
        resetRequest.usedAt = currentTime
        passwordResetRequestRepository.save(resetRequest)

        logger.info("Password successfully reset for user: {}", user.email)
    }

    private fun findMatchingResetRequest(
        token: String,
        activeRequests: List<PasswordResetRequest>,
        currentTime: LocalDateTime
    ): PasswordResetRequest? {

        val resetRequest = activeRequests.firstOrNull { request ->
            passwordEncoder.matches(token, request.token)
        }

        if (resetRequest != null) {
            return resetRequest
        }

        logTokenValidationFailure(token, currentTime)
        return null
    }

    private fun logTokenValidationFailure(token: String, currentTime: LocalDateTime) {
        val allRequests = passwordResetRequestRepository.findAll()
        val matchingRequest = allRequests.firstOrNull { request ->
            passwordEncoder.matches(token, request.token)
        }

        when {
            matchingRequest == null -> {
                logger.warn("Password reset failed: Token does not exist or hash doesn't match")
            }
            matchingRequest.used -> {
                logger.warn(
                    "Password reset failed: Token has already been used (usedAt: {})",
                    matchingRequest.usedAt
                )
            }
            matchingRequest.expiresAt.isBefore(currentTime) -> {
                logger.warn(
                    "Password reset failed: Token has expired (expiresAt: {}, currentTime: {})",
                    matchingRequest.expiresAt,
                    currentTime
                )
            }
            else -> {
                logger.warn("Password reset failed: Token is invalid for unknown reason")
            }
        }
    }
}
