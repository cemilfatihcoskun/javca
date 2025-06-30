package com.sstek.javca.domain.model

import com.sstek.javca.data.model.CallRequestDto

data class CallRequest(
    val callerId: String = "",
    val calleeId: String = "",
    val timestamp: Long = 0,
    val status: CallStatus = CallStatus.PENDING
)
