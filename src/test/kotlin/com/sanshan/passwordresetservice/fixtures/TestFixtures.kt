package com.sanshan.passwordresetservice.fixtures

import com.sanshan.passwordresetservice.entity.PasswordResetRequest
import com.sanshan.passwordresetservice.entity.User
import java.time.LocalDateTime

object TestFixtures {

    const val TEST_EMAIL = "test@example.com"
    const val UNIQUE_EMAIL = "unique@example.com"
    const val EXISTS_EMAIL = "exists@example.com"
    const val NONEXISTENT_EMAIL = "nonexistent@example.com"
    const val TEST_TOKEN = "test-token-uuid"
    const val UNIQUE_TOKEN = "unique-token"
    const val TEST_REPO_TOKEN = "test-token"
    const val TEST_PASSWORD = "NewPassword123"
    const val HASHED_PASSWORD = "\$2a\$10\$hashedPassword"
    const val TEST_USER_ID = 1L
    
    fun createTestUser(
        id: Long? = TEST_USER_ID,
        email: String = TEST_EMAIL,
        passwordHash: String = HASHED_PASSWORD,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now()
    ): User {
        return User(
            id = id,
            email = email,
            passwordHash = passwordHash,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    fun createPasswordResetRequest(
        id: Long? = 1L,
        user: User = createTestUser(),
        token: String = TEST_TOKEN,
        createdAt: LocalDateTime = LocalDateTime.now(),
        expiresAt: LocalDateTime = LocalDateTime.now().plusMinutes(30),
        used: Boolean = false,
        usedAt: LocalDateTime? = null
    ): PasswordResetRequest {
        return PasswordResetRequest(
            id = id,
            user = user,
            token = token,
            createdAt = createdAt,
            expiresAt = expiresAt,
            used = used,
            usedAt = usedAt
        )
    }
    
    fun createExpiredPasswordResetRequest(
        user: User = createTestUser(),
        token: String = TEST_TOKEN
    ): PasswordResetRequest {
        return createPasswordResetRequest(
            user = user,
            token = token,
            expiresAt = LocalDateTime.now().minusMinutes(1)
        )
    }
    
    fun createUsedPasswordResetRequest(
        user: User = createTestUser(),
        token: String = TEST_TOKEN
    ): PasswordResetRequest {
        return createPasswordResetRequest(
            user = user,
            token = token,
            used = true,
            usedAt = LocalDateTime.now().minusMinutes(5)
        )
    }
}
