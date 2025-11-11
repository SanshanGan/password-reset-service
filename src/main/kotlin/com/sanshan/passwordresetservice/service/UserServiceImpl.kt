package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.entity.User
import com.sanshan.passwordresetservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun findByEmail(email: String): User? {
        logger.debug("Finding user by email: {}", email)
        return userRepository.findByEmail(email)
    }

    @Transactional
    override fun updatePassword(userId: Long, newPassword: String) {
        logger.info("Updating password for user ID: {}", userId)
        
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with ID: $userId") }
        
        val hashedPassword = passwordEncoder.encode(newPassword)
        user.passwordHash = hashedPassword
        user.updatedAt = LocalDateTime.now()
        
        userRepository.save(user)
        logger.info("Password successfully updated for user ID: {}", userId)
    }
}
