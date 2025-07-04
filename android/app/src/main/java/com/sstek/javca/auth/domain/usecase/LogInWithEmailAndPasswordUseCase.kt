package com.sstek.javca.auth.domain.usecase

import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LogInWithEmailAndPasswordUseCase @Inject constructor(
    val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): User? {
        return authRepository.loginWithEmailAndPassword(email, password)
    }
}