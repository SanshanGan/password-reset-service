package com.sanshan.passwordresetservice.exception

sealed class PasswordResetException(message: String) : RuntimeException(message)

class InvalidEmailException(email: String) :
    PasswordResetException("Invalid email format: $email")

class UserNotFoundException(email: String) :
    PasswordResetException("User not found: $email")

class ActiveRequestExistsException(email: String) :
    PasswordResetException("Active password reset request already exists for: $email")

class InvalidTokenException :
    PasswordResetException("Invalid or expired reset token")

class TokenAlreadyUsedException :
    PasswordResetException("Reset token has already been used")
