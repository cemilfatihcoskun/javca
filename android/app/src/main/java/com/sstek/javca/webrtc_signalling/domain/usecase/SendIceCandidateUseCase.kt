package com.sstek.javca.webrtc_signalling.domain.usecase

import com.sstek.javca.webrtc_signalling.domain.entity.IceCandidateData
import com.sstek.javca.webrtc_signalling.domain.repository.SignalingRepository
import javax.inject.Inject

class SendIceCandidateUseCase @Inject constructor(
    val repo: SignalingRepository
) {
    suspend operator fun invoke(callId: String, iceCandidateData: IceCandidateData) = repo.sendIceCandidate(callId, iceCandidateData)
}