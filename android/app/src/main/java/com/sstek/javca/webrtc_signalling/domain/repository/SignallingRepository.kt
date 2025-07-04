package com.sstek.javca.webrtc_signalling.domain.repository

import com.sstek.javca.webrtc_signalling.domain.entity.IceCandidateData
import com.sstek.javca.webrtc_signalling.domain.entity.SdpOffer

interface SignalingRepository {
    suspend fun sendSdp(callId: String, sdp: SdpOffer)
    suspend fun sendIceCandidate(callId: String, candidate: IceCandidateData)
    fun observeRemoteSdp(callId: String, onReceived: (SdpOffer) -> Unit)
    fun observeRemoteIceCandidates(callId: String, onReceived: (IceCandidateData) -> Unit)
    fun clearSignalingData(callId: String)
}
