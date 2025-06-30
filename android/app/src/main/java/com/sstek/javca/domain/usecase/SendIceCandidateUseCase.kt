package com.sstek.javca.domain.usecase

import com.sstek.javca.domain.model.IceCandidateData
import com.sstek.javca.domain.model.SdpOffer
import com.sstek.javca.domain.repository.SignalingRepository
import javax.inject.Inject

class SendIceCandidateUseCase @Inject constructor(
    val repo: SignalingRepository
) {
    suspend operator fun invoke(callId: String, iceCandidateData: IceCandidateData) = repo.sendIceCandidate(callId, iceCandidateData)
}