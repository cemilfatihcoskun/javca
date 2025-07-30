package com.sstek.javca.call.domain.usecase

import com.sstek.javca.call.domain.entity.Call
import com.sstek.javca.call.domain.repository.CallObserverRepository
import javax.inject.Inject

class ObserveCallRequestUseCase @Inject constructor(
    private val callObserverRepository: CallObserverRepository
) {
    suspend operator fun invoke(callId: String,  onCallReceived: (callId: String, call: Call) -> Unit) = callObserverRepository.listenToCallDetails(callId, onCallReceived)
}