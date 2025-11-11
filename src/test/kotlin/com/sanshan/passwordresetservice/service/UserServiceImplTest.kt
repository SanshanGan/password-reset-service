package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.entity.User
import com.sanshan.passwordresetservice.fixtures.TestFixtures
import com.sanshan.passwordresetservice.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

class UserServiceImplTest {

    private lateinit var userService: UserServiceImpl
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setup() {
        userRepository = mock(UserRepository::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)

        userService = UserServiceImpl(userRepository, passwordEncoder)
    }

    @Test
    fun `findByEmail should return user when exists`() {
        val user = TestFixtures.createTestUser()

        whenever(userRepository.findByEmail(TestFixtures.TEST_EMAIL)).thenReturn(user)

        val result = userService.findByEmail(TestFixtures.TEST_EMAIL)

        assertNotNull(result)
        assertEquals(TestFixtures.TEST_EMAIL, result?.email)
        verify(userRepository).findByEmail(TestFixtures.TEST_EMAIL)
    }

    @Test
    fun `findByEmail should return null when user does not exist`() {
        whenever(userRepository.findByEmail(TestFixtures.TEST_EMAIL)).thenReturn(null)

        val result = userService.findByEmail(TestFixtures.TEST_EMAIL)

        assertNull(result)
        verify(userRepository).findByEmail(TestFixtures.TEST_EMAIL)
    }

    @Test
    fun `updatePassword should hash password and update user`() {
        val user = TestFixtures.createTestUser()
        val userCaptor = argumentCaptor<User>()

        whenever(userRepository.findById(TestFixtures.TEST_USER_ID)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.encode(TestFixtures.TEST_PASSWORD)).thenReturn(TestFixtures.HASHED_PASSWORD)
        whenever(userRepository.save(userCaptor.capture())).thenAnswer { userCaptor.firstValue }

        userService.updatePassword(TestFixtures.TEST_USER_ID, TestFixtures.TEST_PASSWORD)

        assertEquals(TestFixtures.HASHED_PASSWORD, userCaptor.firstValue.passwordHash)
        verify(passwordEncoder).encode(TestFixtures.TEST_PASSWORD)
        verify(userRepository).save(any())
    }

    @Test
    fun `updatePassword should throw exception when user not found`() {
        val nonExistentUserId = 999L

        whenever(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty())

        assertThrows<IllegalArgumentException> {
            userService.updatePassword(nonExistentUserId, TestFixtures.TEST_PASSWORD)
        }

        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `updatePassword should update updatedAt timestamp`() {
        val oldTimestamp = LocalDateTime.now().minusDays(1)
        val user = TestFixtures.createTestUser(updatedAt = oldTimestamp)
        val userCaptor = argumentCaptor<User>()

        whenever(userRepository.findById(TestFixtures.TEST_USER_ID)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.encode(TestFixtures.TEST_PASSWORD)).thenReturn(TestFixtures.HASHED_PASSWORD)
        whenever(userRepository.save(userCaptor.capture())).thenAnswer { userCaptor.firstValue }

        userService.updatePassword(TestFixtures.TEST_USER_ID, TestFixtures.TEST_PASSWORD)

        assertTrue(userCaptor.firstValue.updatedAt.isAfter(oldTimestamp))
        verify(userRepository).save(any())
    }
}
