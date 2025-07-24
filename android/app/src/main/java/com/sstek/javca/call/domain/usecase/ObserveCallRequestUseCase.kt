package com.sstek.javca.call.domain.usecase

import com.sstek.javca.call.domain.entity.CallRequest
import com.sstek.javca.call.domain.repository.CallObserverRepository
import javax.inject.Inject

class ObserveCallRequestUseCase @Inject constructor(
    private val callObserverRepository: CallObserverRepository
) {
    suspend operator fun invoke(callId: String,  onCallReceived: (callId: String, callRequest: CallRequest) -> Unit) = callObserverRepository.listenToCallDetails(callId, onCallReceived)
}