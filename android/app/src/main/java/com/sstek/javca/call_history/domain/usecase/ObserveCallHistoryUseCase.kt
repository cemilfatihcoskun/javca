package com.sstek.javca.call_history.domain.usecase

import com.sstek.javca.call.domain.entity.Call
import com.sstek.javca.call_history.domain.repository.CallHistoryRepository
import com.sstek.javca.call_history.domain.repository.ListenerHandle
import javax.inject.Inject

class ObserveCallHistoryUseCase @Inject constructor(
    private val repository: CallHistoryRepository
) {
    operator fun invoke(
        userId: String,
        onUpdated: (List<Call>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerHandle {
        return repository.observeCallHistoryForUser(userId, onUpdated, onError)
    }
}
