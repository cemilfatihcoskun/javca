package com.sstek.javca.user.domain.repository

import com.sstek.javca.user.domain.entity.User

interface UserRepository {
    fun getAllUsers(onUsersUpdated: (List<User>) -> Unit)
    suspend fun getUserById(uid: String): User?
}