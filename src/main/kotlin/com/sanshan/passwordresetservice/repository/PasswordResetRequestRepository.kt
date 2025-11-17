package com.sanshan.passwordresetservice.repository

import com.sanshan.passwordresetservice.entity.PasswordResetRequest
import com.sanshan.passwordresetservice.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PasswordResetRequestRepository : JpaRepository<PasswordResetRequest, Long> {
    fun existsByUserAndUsedFalseAndExpiresAtAfter(
        user: User,
        currentTime: LocalDateTime
    ): Boolean
    
    fun findAllByUsedFalseAndExpiresAtAfter(
        currentTime: LocalDateTime
    ): List<PasswordResetRequest>
}
