package com.sanshan.passwordresetservice.exception

import com.sanshan.passwordresetservice.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(InvalidEmailException::class)
    fun handleInvalidEmail(ex: InvalidEmailException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid email: {}", ex.message)
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse("INVALID_EMAIL", ex.message ?: "Invalid email"))
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("User not found: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("USER_NOT_FOUND", ex.message ?: "User not found"))
    }

    @ExceptionHandler(ActiveRequestExistsException::class)
    fun handleActiveRequestExists(ex: ActiveRequestExistsException): ResponseEntity<ErrorResponse> {
        logger.warn("Active request exists: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse("ACTIVE_REQUEST_EXISTS", ex.message ?: "Active request exists"))
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(ex: InvalidTokenException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid token: {}", ex.message)
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse("INVALID_TOKEN", ex.message ?: "Invalid token"))
    }

    @ExceptionHandler(TokenAlreadyUsedException::class)
    fun handleTokenAlreadyUsed(ex: TokenAlreadyUsedException): ResponseEntity<ErrorResponse> {
        logger.warn("Token already used: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse("TOKEN_ALREADY_USED", ex.message ?: "Token already used"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        logger.warn("Validation failed: {}", errors)
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse("VALIDATION_ERROR", errors))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
    }
}
