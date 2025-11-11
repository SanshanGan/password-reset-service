package com.sanshan.passwordresetservice.repository

import com.sanshan.passwordresetservice.fixtures.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun `findByEmail should return user when exists`() {
        val user = TestFixtures.createTestUser(id = null, email = TestFixtures.TEST_EMAIL)
        entityManager.persist(user)
        entityManager.flush()

        val found = userRepository.findByEmail(TestFixtures.TEST_EMAIL)

        assertNotNull(found)
        assertEquals(TestFixtures.TEST_EMAIL, found?.email)
    }

    @Test
    fun `findByEmail should return null when user does not exist`() {
        val found = userRepository.findByEmail(TestFixtures.NONEXISTENT_EMAIL)

        assertNull(found)
    }

    @Test
    fun `existsByEmail should return true when user exists`() {
        val user = TestFixtures.createTestUser(id = null, email = TestFixtures.EXISTS_EMAIL)
        entityManager.persist(user)
        entityManager.flush()

        val exists = userRepository.existsByEmail(TestFixtures.EXISTS_EMAIL)

        assertTrue(exists)
    }

    @Test
    fun `existsByEmail should return false when user does not exist`() {
        val exists = userRepository.existsByEmail(TestFixtures.NONEXISTENT_EMAIL)

        assertFalse(exists)
    }

    @Test
    fun `should enforce unique email constraint`() {
        val user1 = TestFixtures.createTestUser(id = null, email = TestFixtures.UNIQUE_EMAIL)
        entityManager.persist(user1)
        entityManager.flush()

        val user2 = TestFixtures.createTestUser(id = null, email = TestFixtures.UNIQUE_EMAIL)

        assertThrows(Exception::class.java) {
            entityManager.persist(user2)
            entityManager.flush()
        }
    }
}
