package com.sstek.javca.webrtc_signalling.domain.entity

data class IceCandidateData(
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val candidate: String = ""
)

