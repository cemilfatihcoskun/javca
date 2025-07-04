package com.sstek.javca.webrtc_signalling.domain.usecase

import com.sstek.javca.webrtc_signalling.domain.entity.SdpOffer
import com.sstek.javca.webrtc_signalling.domain.repository.SignalingRepository
import javax.inject.Inject

class SendSdpUseCase @Inject constructor(
    val repo: SignalingRepository
) {
    suspend operator fun invoke(callId: String, sdpOffer: SdpOffer) = repo.sendSdp(callId, sdpOffer)
}