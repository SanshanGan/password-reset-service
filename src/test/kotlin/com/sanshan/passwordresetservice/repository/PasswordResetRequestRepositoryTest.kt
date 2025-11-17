package com.sanshan.passwordresetservice.repository

import com.sanshan.passwordresetservice.fixtures.TestFixtures.createPasswordResetRequest
import com.sanshan.passwordresetservice.fixtures.TestFixtures.createTestUser
import com.sanshan.passwordresetservice.fixtures.TestFixtures.UNIQUE_TOKEN
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.LocalDateTime

@DataJpaTest
class PasswordResetRequestRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var passwordResetRequestRepository: PasswordResetRequestRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        passwordResetRequestRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `existsByUserAndUsedFalseAndExpiresAtAfter should return true for active request`() {
        val user = createTestUser(id = null)
        entityManager.persist(user)
        
        val activeRequest = createPasswordResetRequest(
            id = null,
            user = user,
            used = false,
            expiresAt = LocalDateTime.now().plusMinutes(30)
        )
        entityManager.persist(activeRequest)
        entityManager.flush()

        val exists = passwordResetRequestRepository.existsByUserAndUsedFalseAndExpiresAtAfter(
            user,
            LocalDateTime.now()
        )

        assertTrue(exists)
    }

    @Test
    fun `existsByUserAndUsedFalseAndExpiresAtAfter should return false when no active request`() {
        val user = createTestUser(id = null)
        entityManager.persist(user)
        entityManager.flush()

        val exists = passwordResetRequestRepository.existsByUserAndUsedFalseAndExpiresAtAfter(
            user,
            LocalDateTime.now()
        )

        assertFalse(exists)
    }

    @Test
    fun `should enforce unique token constraint`() {
        val user = createTestUser(id = null)
        entityManager.persist(user)
        
        val request1 = createPasswordResetRequest(id = null, user = user, token = UNIQUE_TOKEN)
        entityManager.persist(request1)
        entityManager.flush()

        val request2 = createPasswordResetRequest(id = null, user = user, token = UNIQUE_TOKEN)

        assertThrows(Exception::class.java) {
            entityManager.persist(request2)
            entityManager.flush()
        }
    }

}
