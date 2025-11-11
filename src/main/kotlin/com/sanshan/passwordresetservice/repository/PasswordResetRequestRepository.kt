package com.sanshan.passwordresetservice.repository

import com.sanshan.passwordresetservice.entity.PasswordResetRequest
import com.sanshan.passwordresetservice.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PasswordResetRequestRepository : JpaRepository<PasswordResetRequest, Long> {
    fun findByToken(token: String): PasswordResetRequest?
    
    fun findByUserAndUsedFalseAndExpiresAtAfter(
        user: User,
        currentTime: LocalDateTime
    ): PasswordResetRequest?
    
    fun existsByUserAndUsedFalseAndExpiresAtAfter(
        user: User,
        currentTime: LocalDateTime
    ): Boolean
}
