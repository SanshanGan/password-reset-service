package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.entity.User
import com.sanshan.passwordresetservice.fixtures.TestFixtures.createTestUser
import com.sanshan.passwordresetservice.fixtures.TestFixtures.HASHED_PASSWORD
import com.sanshan.passwordresetservice.fixtures.TestFixtures.TEST_EMAIL
import com.sanshan.passwordresetservice.fixtures.TestFixtures.TEST_PASSWORD
import com.sanshan.passwordresetservice.fixtures.TestFixtures.TEST_USER_ID
import com.sanshan.passwordresetservice.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional

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
        val user = createTestUser()

        whenever(userRepository.findByEmail(TEST_EMAIL)).thenReturn(user)

        val result = userService.findByEmail(TEST_EMAIL)

        assertNotNull(result)
        assertEquals(TEST_EMAIL, result?.email)
        verify(userRepository).findByEmail(TEST_EMAIL)
    }

    @Test
    fun `findByEmail should return null when user does not exist`() {
        whenever(userRepository.findByEmail(TEST_EMAIL)).thenReturn(null)

        val result = userService.findByEmail(TEST_EMAIL)

        assertNull(result)
        verify(userRepository).findByEmail(TEST_EMAIL)
    }

    @Test
    fun `updatePassword should hash password and update user`() {
        val user = createTestUser()
        val userCaptor = argumentCaptor<User>()

        whenever(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD)
        whenever(userRepository.save(userCaptor.capture())).thenAnswer { userCaptor.firstValue }

        userService.updatePassword(TEST_USER_ID, TEST_PASSWORD)

        assertEquals(HASHED_PASSWORD, userCaptor.firstValue.passwordHash)
        verify(passwordEncoder).encode(TEST_PASSWORD)
        verify(userRepository).save(any())
    }

    @Test
    fun `updatePassword should throw exception when user not found`() {
        val nonExistentUserId = 999L

        whenever(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty())

        assertThrows<IllegalArgumentException> {
            userService.updatePassword(nonExistentUserId, TEST_PASSWORD)
        }

        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `updatePassword should update updatedAt timestamp`() {
        val oldTimestamp = LocalDateTime.now().minusDays(1)
        val user = createTestUser(updatedAt = oldTimestamp)
        val userCaptor = argumentCaptor<User>()

        whenever(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD)
        whenever(userRepository.save(userCaptor.capture())).thenAnswer { userCaptor.firstValue }

        userService.updatePassword(TEST_USER_ID, TEST_PASSWORD)

        assertTrue(userCaptor.firstValue.updatedAt.isAfter(oldTimestamp))
        verify(userRepository).save(any())
    }
}
