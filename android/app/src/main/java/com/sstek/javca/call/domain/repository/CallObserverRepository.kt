package com.sstek.javca.call.domain.repository

import com.sstek.javca.call.domain.entity.CallRequest

interface CallObserverRepository {
    fun observeIncomingCalls(userId: String, onCallReceived: (callId: String, callRequest: CallRequest) -> Unit)
    fun listenToCallDetails(callId: String, onCallReceived: (callId: String, callRequest: CallRequest) -> Unit)

    fun removeCallDetailsListener(callId: String)
    fun removeListener(userId: String)
}