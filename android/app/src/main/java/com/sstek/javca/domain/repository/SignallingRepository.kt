package com.sstek.javca.domain.repository

import com.sstek.javca.domain.model.IceCandidateData
import com.sstek.javca.domain.model.SdpOffer

interface SignalingRepository {
    suspend fun sendSdp(callId: String, sdp: SdpOffer)
    suspend fun sendIceCandidate(callId: String, candidate: IceCandidateData)
    fun observeRemoteSdp(callId: String, onReceived: (SdpOffer) -> Unit)
    fun observeRemoteIceCandidates(callId: String, onReceived: (IceCandidateData) -> Unit)
    fun clearSignalingData(callId: String)
}
