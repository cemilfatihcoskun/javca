package com.sstek.javca.domain.usecase

import com.sstek.javca.domain.model.User
import com.sstek.javca.domain.repository.UserRepository
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    val userRepository: UserRepository
) {
    suspend operator fun invoke(): List<User> {
        return userRepository.getAllUsers()
    }
}