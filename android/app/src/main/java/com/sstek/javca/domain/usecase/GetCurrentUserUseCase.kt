package com.sstek.javca.domain.usecase

import com.google.firebase.auth.FirebaseUser
import com.sstek.javca.domain.model.User
import com.sstek.javca.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    val authRepository: AuthRepository
) {
    operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}