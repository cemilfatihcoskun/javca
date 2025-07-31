package com.sstek.javca.server_connection.domain.repository

interface ServerConnectionRepository {
    fun observeConnectionStatus(callback: (Boolean) -> Unit)

    suspend fun isServerConnected(): Boolean
}
