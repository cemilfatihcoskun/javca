package com.sstek.javca.domain.repository

import com.sstek.javca.domain.model.CallRequest
import com.sstek.javca.domain.model.CallStatus

interface CallRepository {
    suspend fun sendCallRequest(callRequest: CallRequest): Pair<String?, CallStatus>
    suspend fun updateCallStatus(callId: String, status: CallStatus)
    suspend fun observeCall(calleeId: String, onCallReceived: (CallRequest) -> Unit)
}