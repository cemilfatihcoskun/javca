package com.sstek.javca.call_history.domain.repository

import com.sstek.javca.call.domain.entity.Call

interface CallHistoryRepository {
    suspend fun getCallHistoryForUser(userId: String): List<Call>

    fun observeCallHistoryForUser(
        userId: String,
        onUpdated: (List<Call>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerHandle
}

interface ListenerHandle {
    fun remove()
}
