package com.sstek.javca.auth.domain.usecase

import com.sstek.javca.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LogOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.logOut()
}