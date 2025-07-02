package com.sstek.javca.domain.repository

import com.sstek.javca.domain.model.User

interface UserRepository {
    fun getAllUsers(onUsersUpdated: (List<User>) -> Unit)
    suspend fun getUserById(uid: String): User?
}