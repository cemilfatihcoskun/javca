package com.sstek.javca.domain.repository

import com.sstek.javca.domain.model.User

interface UserRepository {
    suspend fun getAllUsers(): List<User>
    suspend fun getUserById(uid: String): User?
}