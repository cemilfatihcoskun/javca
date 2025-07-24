package com.sstek.javca.auth.domain.usecase

import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterWithUsernameAndEmailAndPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, email: String, password: String): User? {
        return authRepository.registerWithUsernameAndEmailAndPassword(username, email, password)
    }
}