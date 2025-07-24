package com.sstek.javca.auth.domain.repository

import com.sstek.javca.user.domain.entity.User

interface AuthRepository {
    suspend fun loginWithEmailAndPassword(email: String, password: String): User?
    suspend fun registerWithUsernameAndEmailAndPassword(username: String, email: String, password: String): User?

    suspend fun getCurrentUser(): User?
    suspend fun reloadAuth(): User?
    suspend fun logOut()

}