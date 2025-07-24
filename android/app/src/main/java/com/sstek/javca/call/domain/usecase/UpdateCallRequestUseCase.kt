package com.sstek.javca.call.domain.usecase

import com.sstek.javca.call.domain.entity.CallStatus
import com.sstek.javca.call.domain.repository.CallRepository
import javax.inject.Inject

class UpdateCallRequestUseCase @Inject constructor(
    private val repository: CallRepository
) {
    suspend operator fun invoke(callId: String, status: CallStatus) {
        repository.updateCallStatus(callId, status)
    }
}