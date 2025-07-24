package com.sstek.javca.auth.domain.usecase

import com.sstek.javca.auth.domain.repository.AuthRepository
import com.sstek.javca.user.domain.entity.User
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}