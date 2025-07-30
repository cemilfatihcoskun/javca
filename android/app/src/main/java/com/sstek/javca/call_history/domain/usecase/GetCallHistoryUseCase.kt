package com.sstek.javca.call_history.domain.usecase

import com.sstek.javca.call.domain.entity.Call
import com.sstek.javca.call_history.domain.repository.CallHistoryRepository

import javax.inject.Inject

class GetCallHistoryUseCase @Inject constructor(
    val callHistoryRepository: CallHistoryRepository
) {
    suspend operator fun invoke(userId: String): List<Call> {
        return callHistoryRepository.getCallHistoryForUser(userId)
    }
}