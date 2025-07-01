package com.sstek.javca.domain.usecase

import com.sstek.javca.domain.model.CallRequest
import com.sstek.javca.domain.repository.CallObserverRepository
import com.sstek.javca.domain.repository.CallRepository
import javax.inject.Inject

class ObserveCallRequestUseCase @Inject constructor(
    private val callObserverRepository: CallObserverRepository
) {
    suspend operator fun invoke(callId: String,  onCallReceived: (callId: String, callRequest: CallRequest) -> Unit) = callObserverRepository.listenToCallDetails(callId, onCallReceived)
}