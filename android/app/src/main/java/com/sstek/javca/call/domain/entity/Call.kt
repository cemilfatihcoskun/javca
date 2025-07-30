package com.sstek.javca.call.domain.entity

data class Call(
    val callerId: String = "",
    val calleeId: String = "",
    val timestamp: Long = 0,
    val status: CallStatus = CallStatus.PENDING
)
