package com.sanshan.passwordresetservice.service

import com.sanshan.passwordresetservice.entity.User

interface UserService {
    fun findByEmail(email: String): User?
    fun updatePassword(userId: Long, newPassword: String)
}
