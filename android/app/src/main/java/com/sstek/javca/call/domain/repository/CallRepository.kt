package com.sstek.javca.call.domain.repository

import com.sstek.javca.call.domain.entity.CallRequest
import com.sstek.javca.call.domain.entity.CallStatus

interface CallRepository {
    suspend fun sendCallRequest(callRequest: CallRequest): Pair<String?, CallStatus>
    suspend fun updateCallStatus(callId: String, status: CallStatus)
}