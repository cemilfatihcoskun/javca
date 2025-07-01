package com.sstek.javca.domain.usecase

import com.sstek.javca.domain.model.CallRequest
import com.sstek.javca.domain.model.CallStatus
import com.sstek.javca.domain.repository.CallRepository
import javax.inject.Inject

class SendCallRequestUseCase @Inject constructor(
    private val repository: CallRepository
) {
    suspend operator fun invoke(callRequest: CallRequest): Pair<String?, CallStatus> {
        return repository.sendCallRequest(callRequest)
    }
}