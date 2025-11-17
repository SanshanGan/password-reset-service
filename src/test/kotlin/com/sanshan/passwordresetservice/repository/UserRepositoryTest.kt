package com.sanshan.passwordresetservice.repository

import com.sanshan.passwordresetservice.fixtures.TestFixtures.createTestUser
import com.sanshan.passwordresetservice.fixtures.TestFixtures.NONEXISTENT_EMAIL
import com.sanshan.passwordresetservice.fixtures.TestFixtures.TEST_EMAIL
import com.sanshan.passwordresetservice.fixtures.TestFixtures.UNIQUE_EMAIL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
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
        val user = createTestUser(id = null, email = TEST_EMAIL)
        entityManager.persist(user)
        entityManager.flush()

        val found = userRepository.findByEmail(TEST_EMAIL)

        assertNotNull(found)
        assertEquals(TEST_EMAIL, found?.email)
    }

    @Test
    fun `findByEmail should return null when user does not exist`() {
        val found = userRepository.findByEmail(NONEXISTENT_EMAIL)

        assertNull(found)
    }

    @Test
    fun `should enforce unique email constraint`() {
        val user1 = createTestUser(id = null, email = UNIQUE_EMAIL)
        entityManager.persist(user1)
        entityManager.flush()

        val user2 = createTestUser(id = null, email = UNIQUE_EMAIL)

        assertThrows(Exception::class.java) {
            entityManager.persist(user2)
            entityManager.flush()
        }
    }
}
