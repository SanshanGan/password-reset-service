package com.sanshan.passwordresetservice.util

import org.springframework.stereotype.Component

@Component
class EmailValidator {

    private val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun isValid(email: String): Boolean {
        return emailPattern.matches(email)
    }
}
