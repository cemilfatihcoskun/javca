package com.sstek.javca.user.domain.usecase

import com.sstek.javca.user.domain.repository.UserRepository
import javax.inject.Inject

class AddFavoriteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(currentUserId: String, favoriteUserId: String) {
        userRepository.addFavoriteUser(currentUserId, favoriteUserId)
    }
}