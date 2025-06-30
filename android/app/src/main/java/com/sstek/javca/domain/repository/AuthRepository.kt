package com.sstek.javca.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.sstek.javca.domain.model.User

interface AuthRepository {
    suspend fun loginWithEmailAndPassword(email: String, password: String): User?
    suspend fun registerWithUsernameAndEmailAndPassword(username: String, email: String, password: String): User?
    suspend fun reloadAuth(): Boolean

    fun logOut(): Unit
    fun getCurrentUser(): User?
}