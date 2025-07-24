package com.sstek.javca.webrtc_signalling.domain.entity

data class SdpOffer(
    val type: SdpType = SdpType.OFFER,
    val sdp: String = ""
)