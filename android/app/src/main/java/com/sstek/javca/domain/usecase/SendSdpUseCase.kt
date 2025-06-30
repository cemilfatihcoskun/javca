package com.sstek.javca.domain.usecase

import com.sstek.javca.data.repository.FirebaseSignalingRepository
import com.sstek.javca.domain.model.SdpOffer
import com.sstek.javca.domain.repository.SignalingRepository
import javax.inject.Inject

class SendSdpUseCase @Inject constructor(
    val repo: SignalingRepository
) {
    suspend operator fun invoke(callId: String, sdpOffer: SdpOffer) = repo.sendSdp(callId, sdpOffer)
}