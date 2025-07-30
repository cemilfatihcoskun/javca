package com.sstek.javca.user.domain.entity

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val favorites: Map<String, Boolean> = emptyMap(),
    val status: String = "offline",
    val lastSeen: Long = 0
)