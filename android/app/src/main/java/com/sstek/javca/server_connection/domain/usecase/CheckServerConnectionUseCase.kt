package com.sstek.javca.server_connection.domain.usecase

import com.sstek.javca.server_connection.domain.repository.ServerConnectionRepository
import javax.inject.Inject

class CheckServerConnectionUseCase @Inject constructor(
    private val repository: ServerConnectionRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.isServerConnected()
    }
}
