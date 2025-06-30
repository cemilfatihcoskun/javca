package com.sstek.javca.data.model

data class CallRequestDto(
    val callerId: String = "",
    val calleeId: String = "",
    val timestamp: Long = System.currentTimeMillis(),

    // TODO("Add a enum class")
    val status: String = "pending"
)

