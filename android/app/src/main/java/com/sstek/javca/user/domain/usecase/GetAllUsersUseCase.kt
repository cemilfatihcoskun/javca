package com.sstek.javca.user.domain.usecase

import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.user.domain.repository.UserRepository
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    val userRepository: UserRepository
) {
    suspend operator fun invoke(onUsersUpdated: (List<User>) -> Unit) = userRepository.getAllUsers(onUsersUpdated)
}