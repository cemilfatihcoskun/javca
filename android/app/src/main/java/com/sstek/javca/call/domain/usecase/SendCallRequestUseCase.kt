package com.sstek.javca.call.domain.usecase

import com.sstek.javca.call.domain.entity.CallRequest
import com.sstek.javca.call.domain.entity.CallStatus
import com.sstek.javca.call.domain.repository.CallRepository
import javax.inject.Inject

class SendCallRequestUseCase @Inject constructor(
    private val repository: CallRepository
) {
    suspend operator fun invoke(callRequest: CallRequest): Pair<String?, CallStatus> {
        return repository.sendCallRequest(callRequest)
    }
}