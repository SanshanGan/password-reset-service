package com.sanshan.passwordresetservice.util

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TokenGenerator {

    fun generateResetToken(): String {
        return UUID.randomUUID().toString()
    }
}
