package com.sstek.javca.domain.usecase

import com.google.firebase.auth.FirebaseUser
import com.sstek.javca.domain.model.User
import com.sstek.javca.domain.repository.AuthRepository
import javax.inject.Inject

class LogInWithEmailAndPasswordUseCase @Inject constructor(
    val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): User? {
        return authRepository.loginWithEmailAndPassword(email, password)
    }
}