package com.sstek.javca.domain.usecase

import com.google.firebase.auth.FirebaseUser
import com.sstek.javca.domain.model.User
import com.sstek.javca.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterWithUsernameAndEmailAndPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, email: String, password: String): User? {
        return authRepository.registerWithUsernameAndEmailAndPassword(username, email, password)
    }
}