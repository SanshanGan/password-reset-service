package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.dto.PasswordResetExecutionResponse
import com.sanshan.passwordresetservice.dto.PasswordResetResponse
import com.sanshan.passwordresetservice.entity.PasswordResetRequest
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
    private val tokenGenerator: TokenGenerator
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

        val token = tokenGenerator.generateResetToken()
        val expiresAt = currentTime.plusMinutes(30)

        val resetRequest = PasswordResetRequest(
            user = user,
            token = token,
            expiresAt = expiresAt
        )

        passwordResetRequestRepository.save(resetRequest)

        logger.info("Reset token generated for email: {}, expires at: {}", email, expiresAt)

        return PasswordResetResponse(
            resetToken = token,
            expiresAt = expiresAt.format(dateTimeFormatter)
        )
    }

    @Transactional
    override fun executePasswordReset(token: String, newPassword: String): PasswordResetExecutionResponse {
        logger.info("Executing password reset for token: {}...", token.take(8))

        val resetRequest = passwordResetRequestRepository.findByToken(token)
            ?: run {
                logger.warn("Invalid token: token not found")
                throw InvalidTokenException(token)
            }

        val currentTime = LocalDateTime.now()
        if (resetRequest.expiresAt.isBefore(currentTime)) {
            logger.warn("Token expired: token={}, expiresAt={}", token.take(8), resetRequest.expiresAt)
            throw InvalidTokenException(token)
        }

        if (resetRequest.used) {
            logger.warn("Token already used: token={}, usedAt={}", token.take(8), resetRequest.usedAt)
            throw TokenAlreadyUsedException(token)
        }

        val user = resetRequest.user
        userService.updatePassword(user.id!!, newPassword)

        resetRequest.used = true
        resetRequest.usedAt = currentTime
        passwordResetRequestRepository.save(resetRequest)

        logger.info("Password successfully reset for user: {}", user.email)

        return PasswordResetExecutionResponse(
            message = "Password successfully reset"
        )
    }
}
