package com.sstek.javca.domain.usecase

import com.sstek.javca.domain.model.CallRequest
import com.sstek.javca.domain.repository.CallRepository

import javax.inject.Inject

class GetCallHistoryUseCase @Inject constructor(
    val callRepository: CallRepository
) {
    suspend operator fun invoke(userId: String): List<CallRequest> {
        //val callIds = callRepository.getUserCallsId(userId)
        val calls = ArrayList<CallRequest>()
        return calls
    }
}