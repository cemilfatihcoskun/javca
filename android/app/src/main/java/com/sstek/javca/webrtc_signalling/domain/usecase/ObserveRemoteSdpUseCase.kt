package com.sstek.javca.webrtc_signalling.domain.usecase

import com.sstek.javca.webrtc_signalling.domain.entity.SdpOffer
import com.sstek.javca.webrtc_signalling.domain.repository.SignalingRepository
import javax.inject.Inject

class ObserveRemoteSdpUseCase @Inject constructor(
    val repo: SignalingRepository
) {
    suspend operator fun invoke(callId: String, onReceived: (SdpOffer) -> Unit) = repo.observeRemoteSdp(callId, onReceived)
}
