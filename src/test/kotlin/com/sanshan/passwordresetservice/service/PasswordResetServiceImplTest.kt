package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.entity.PasswordResetRequest
import com.sanshan.passwordresetservice.entity.persistedId
import com.sanshan.passwordresetservice.exception.ActiveRequestExistsException
import com.sanshan.passwordresetservice.exception.InvalidEmailException
import com.sanshan.passwordresetservice.exception.InvalidTokenException
import com.sanshan.passwordresetservice.exception.TokenAlreadyUsedException
import com.sanshan.passwordresetservice.exception.UserNotFoundException
import com.sanshan.passwordresetservice.fixtures.TestFixtures.createExpiredPasswordResetRequest
import com.sanshan.passwordresetservice.fixtures.TestFixtures.createPasswordResetRequest
import com.sanshan.passwordresetservice.fixtures.TestFixtures.createTestUser
import com.sanshan.passwordresetservice.fixtures.TestFixtures.createUsedPasswordResetRequest
import com.sanshan.passwordresetservice.fixtures.TestFixtures.TEST_EMAIL
import com.sanshan.passwordresetservice.fixtures.TestFixtures.TEST_PASSWORD
import com.sanshan.passwordresetservice.fixtures.TestFixtures.TEST_TOKEN
import com.sanshan.passwordresetservice.repository.PasswordResetRequestRepository
import com.sanshan.passwordresetservice.util.EmailValidator
import com.sanshan.passwordresetservice.util.TokenGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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

class PasswordResetServiceImplTest {

    private lateinit var passwordResetService: PasswordResetServiceImpl
    private lateinit var userService: UserService
    private lateinit var passwordResetRequestRepository: PasswordResetRequestRepository
    private lateinit var emailValidator: EmailValidator
    private lateinit var tokenGenerator: TokenGenerator
    private lateinit var passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder

    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        passwordResetRequestRepository = mock(PasswordResetRequestRepository::class.java)
        emailValidator = mock(EmailValidator::class.java)
        tokenGenerator = mock(TokenGenerator::class.java)
        passwordEncoder = mock(org.springframework.security.crypto.password.PasswordEncoder::class.java)

        passwordResetService = PasswordResetServiceImpl(
            userService,
            passwordResetRequestRepository,
            emailValidator,
            tokenGenerator,
            passwordEncoder
        )
    }

    @Test
    fun `initiatePasswordReset should return token when valid email and no active request`() {
        val user = createTestUser()
        val requestCaptor = argumentCaptor<PasswordResetRequest>()
        val hashedToken = "hashed_$TEST_TOKEN"

        whenever(emailValidator.isValid(TEST_EMAIL)).thenReturn(true)
        whenever(userService.findByEmail(TEST_EMAIL)).thenReturn(user)
        whenever(passwordResetRequestRepository.existsByUserAndUsedFalseAndExpiresAtAfter(any(), any()))
            .thenReturn(false)
        whenever(tokenGenerator.generateResetToken()).thenReturn(TEST_TOKEN)
        whenever(passwordEncoder.encode(TEST_TOKEN)).thenReturn(hashedToken)
        whenever(passwordResetRequestRepository.save(requestCaptor.capture())).thenAnswer { requestCaptor.firstValue }

        val response = passwordResetService.initiatePasswordReset(TEST_EMAIL)

        assertNotNull(response)
        assertEquals(TEST_TOKEN, response.resetToken) // Response contains raw token
        assertNotNull(response.expiresAt)
        assertEquals(hashedToken, requestCaptor.firstValue.token) // Database stores hashed token
        verify(passwordResetRequestRepository).save(any())
    }

    @Test
    fun `initiatePasswordReset should throw InvalidEmailException when email format is invalid`() {
        whenever(emailValidator.isValid(TEST_EMAIL)).thenReturn(false)

        assertThrows<InvalidEmailException> {
            passwordResetService.initiatePasswordReset(TEST_EMAIL)
        }

        verify(userService, never()).findByEmail(any())
    }

    @Test
    fun `initiatePasswordReset should throw UserNotFoundException when user does not exist`() {
        whenever(emailValidator.isValid(TEST_EMAIL)).thenReturn(true)
        whenever(userService.findByEmail(TEST_EMAIL)).thenReturn(null)

        assertThrows<UserNotFoundException> {
            passwordResetService.initiatePasswordReset(TEST_EMAIL)
        }

        verify(passwordResetRequestRepository, never()).save(any())
    }

    @Test
    fun `initiatePasswordReset should throw ActiveRequestExistsException when active request exists`() {
        val user = createTestUser()

        whenever(emailValidator.isValid(TEST_EMAIL)).thenReturn(true)
        whenever(userService.findByEmail(TEST_EMAIL)).thenReturn(user)
        whenever(passwordResetRequestRepository.existsByUserAndUsedFalseAndExpiresAtAfter(any(), any()))
            .thenReturn(true)

        assertThrows<ActiveRequestExistsException> {
            passwordResetService.initiatePasswordReset(TEST_EMAIL)
        }

        verify(tokenGenerator, never()).generateResetToken()
        verify(passwordResetRequestRepository, never()).save(any())
    }

    @Test
    fun `executePasswordReset should succeed with valid token`() {
        val user = createTestUser()
        val resetRequest = createPasswordResetRequest(user = user)
        val requestCaptor = argumentCaptor<PasswordResetRequest>()

        whenever(passwordResetRequestRepository.findAllByUsedFalseAndExpiresAtAfter(any()))
            .thenReturn(listOf(resetRequest))
        whenever(passwordEncoder.matches(TEST_TOKEN, resetRequest.token)).thenReturn(true)
        whenever(passwordResetRequestRepository.save(requestCaptor.capture())).thenAnswer { requestCaptor.firstValue }

        val response = passwordResetService.executePasswordReset(TEST_TOKEN, TEST_PASSWORD)

        assertNotNull(response)
        assertEquals("Password successfully reset", response.message)
        assertTrue(requestCaptor.firstValue.used)
        assertNotNull(requestCaptor.firstValue.usedAt)
        verify(userService).updatePassword(user.persistedId, TEST_PASSWORD)
        verify(passwordResetRequestRepository).save(any())
    }

    @Test
    fun `executePasswordReset should throw InvalidTokenException when token not found`() {
        whenever(passwordResetRequestRepository.findAllByUsedFalseAndExpiresAtAfter(any()))
            .thenReturn(emptyList())

        assertThrows<InvalidTokenException> {
            passwordResetService.executePasswordReset(TEST_TOKEN, TEST_PASSWORD)
        }

        verify(userService, never()).updatePassword(any(), any())
    }

    @Test
    fun `executePasswordReset should throw InvalidTokenException when token is expired`() {
        // Expired tokens won't be returned by findAllByUsedFalseAndExpiresAtAfter
        whenever(passwordResetRequestRepository.findAllByUsedFalseAndExpiresAtAfter(any()))
            .thenReturn(emptyList())

        assertThrows<InvalidTokenException> {
            passwordResetService.executePasswordReset(TEST_TOKEN, TEST_PASSWORD)
        }

        verify(userService, never()).updatePassword(any(), any())
    }

    @Test
    fun `executePasswordReset should throw InvalidTokenException when token already used`() {
        // Used tokens won't be returned by findAllByUsedFalseAndExpiresAtAfter
        whenever(passwordResetRequestRepository.findAllByUsedFalseAndExpiresAtAfter(any()))
            .thenReturn(emptyList())

        assertThrows<InvalidTokenException> {
            passwordResetService.executePasswordReset(TEST_TOKEN, TEST_PASSWORD)
        }

        verify(userService, never()).updatePassword(any(), any())
    }
}
