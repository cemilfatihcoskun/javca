package com.sstek.javca.domain.usecase

import com.sstek.javca.domain.model.CallStatus
import com.sstek.javca.domain.repository.CallRepository
import javax.inject.Inject

class UpdateCallRequestUseCase @Inject constructor(
    private val repository: CallRepository
) {
    suspend operator fun invoke(callId: String, status: CallStatus) {
        repository.updateCallStatus(callId, status)
    }
}