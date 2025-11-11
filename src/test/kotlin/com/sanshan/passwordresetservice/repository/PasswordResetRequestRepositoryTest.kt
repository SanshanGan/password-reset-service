package com.sanshan.passwordresetservice.repository

import com.sanshan.passwordresetservice.fixtures.TestFixtures
import org.junit.jupiter.api.Assertions.*
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
    fun `findByToken should return request when exists`() {
        val user = TestFixtures.createTestUser(id = null)
        entityManager.persist(user)
        
        val request = TestFixtures.createPasswordResetRequest(id = null, user = user, token = TestFixtures.TEST_REPO_TOKEN)
        entityManager.persist(request)
        entityManager.flush()

        val found = passwordResetRequestRepository.findByToken(TestFixtures.TEST_REPO_TOKEN)

        assertNotNull(found)
        assertEquals(TestFixtures.TEST_REPO_TOKEN, found?.token)
    }

    @Test
    fun `findByToken should return null when token does not exist`() {
        val found = passwordResetRequestRepository.findByToken("nonexistent-token")

        assertNull(found)
    }

    @Test
    fun `findByUserAndUsedFalseAndExpiresAtAfter should return active request`() {
        val user = TestFixtures.createTestUser(id = null)
        entityManager.persist(user)
        
        val activeRequest = TestFixtures.createPasswordResetRequest(
            id = null,
            user = user,
            used = false,
            expiresAt = LocalDateTime.now().plusMinutes(30)
        )
        entityManager.persist(activeRequest)
        entityManager.flush()

        val found = passwordResetRequestRepository.findByUserAndUsedFalseAndExpiresAtAfter(
            user,
            LocalDateTime.now()
        )

        assertNotNull(found)
        assertFalse(found!!.used)
        assertTrue(found.expiresAt.isAfter(LocalDateTime.now()))
    }

    @Test
    fun `findByUserAndUsedFalseAndExpiresAtAfter should not return expired request`() {
        val user = TestFixtures.createTestUser(id = null)
        entityManager.persist(user)
        
        val expiredRequest = TestFixtures.createPasswordResetRequest(
            id = null,
            user = user,
            used = false,
            expiresAt = LocalDateTime.now().minusMinutes(1)
        )
        entityManager.persist(expiredRequest)
        entityManager.flush()

        val found = passwordResetRequestRepository.findByUserAndUsedFalseAndExpiresAtAfter(
            user,
            LocalDateTime.now()
        )

        assertNull(found)
    }

    @Test
    fun `findByUserAndUsedFalseAndExpiresAtAfter should not return used request`() {
        val user = TestFixtures.createTestUser(id = null)
        entityManager.persist(user)
        
        val usedRequest = TestFixtures.createPasswordResetRequest(
            id = null,
            user = user,
            used = true,
            expiresAt = LocalDateTime.now().plusMinutes(30)
        )
        entityManager.persist(usedRequest)
        entityManager.flush()

        val found = passwordResetRequestRepository.findByUserAndUsedFalseAndExpiresAtAfter(
            user,
            LocalDateTime.now()
        )

        assertNull(found)
    }

    @Test
    fun `existsByUserAndUsedFalseAndExpiresAtAfter should return true for active request`() {
        val user = TestFixtures.createTestUser(id = null)
        entityManager.persist(user)
        
        val activeRequest = TestFixtures.createPasswordResetRequest(
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
        val user = TestFixtures.createTestUser(id = null)
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
        val user = TestFixtures.createTestUser(id = null)
        entityManager.persist(user)
        
        val request1 = TestFixtures.createPasswordResetRequest(id = null, user = user, token = TestFixtures.UNIQUE_TOKEN)
        entityManager.persist(request1)
        entityManager.flush()

        val request2 = TestFixtures.createPasswordResetRequest(id = null, user = user, token = TestFixtures.UNIQUE_TOKEN)

        assertThrows(Exception::class.java) {
            entityManager.persist(request2)
            entityManager.flush()
        }
    }

}
