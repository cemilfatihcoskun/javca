package com.sstek.javca.call.domain.repository

import com.sstek.javca.call.domain.entity.Call
import com.sstek.javca.call.domain.entity.CallStatus

interface CallRepository {
    suspend fun sendCallRequest(call: Call): Pair<String?, CallStatus>
    suspend fun updateCallStatus(callId: String, status: CallStatus)
}