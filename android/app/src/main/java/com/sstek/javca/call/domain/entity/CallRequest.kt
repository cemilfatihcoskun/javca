package com.sstek.javca.call.domain.entity

data class CallRequest(
    val callerId: String = "",
    val calleeId: String = "",
    val timestamp: Long = 0,
    val status: CallStatus = CallStatus.PENDING
)
