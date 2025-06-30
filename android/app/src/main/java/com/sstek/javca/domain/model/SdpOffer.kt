package com.sstek.javca.domain.model

data class SdpOffer(
    val type: SdpType = SdpType.OFFER,
    val sdp: String = ""
)