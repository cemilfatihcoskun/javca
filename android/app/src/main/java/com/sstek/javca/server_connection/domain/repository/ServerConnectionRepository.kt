package com.sstek.javca.connection.domain.repository

interface ConnectionRepository {
    fun observeConnectionStatus(callback: (Boolean) -> Unit)
}
