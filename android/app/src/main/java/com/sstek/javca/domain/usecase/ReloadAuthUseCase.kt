package com.sstek.javca.domain.usecase

import com.sstek.javca.domain.model.CallRequest
import com.sstek.javca.domain.repository.AuthRepository
import com.sstek.javca.domain.repository.CallRepository
import javax.inject.Inject

class ReloadAuthUseCase @Inject constructor(
    val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.reloadAuth()
    }
}
