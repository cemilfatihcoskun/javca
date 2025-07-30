package com.sstek.javca.call.domain.repository

import com.sstek.javca.call.domain.entity.Call

interface CallObserverRepository {
    fun observeIncomingCalls(userId: String, onCallReceived: (callId: String, call: Call) -> Unit)
    fun listenToCallDetails(callId: String, onCallReceived: (callId: String, call: Call) -> Unit)

    fun removeCallDetailsListener(callId: String)
    fun removeListener(userId: String)
}