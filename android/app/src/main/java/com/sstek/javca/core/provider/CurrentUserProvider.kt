package com.sstek.javca.core.provider

import com.sstek.javca.user.domain.entity.User

interface CurrentUserProvider {
    suspend fun getCurrentUser(): User?
    //fun getCurrentUserId(): String?
}