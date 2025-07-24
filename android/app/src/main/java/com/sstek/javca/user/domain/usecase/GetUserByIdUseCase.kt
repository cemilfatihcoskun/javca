package com.sstek.javca.user.domain.usecase

import com.sstek.javca.user.domain.repository.UserRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String) = userRepository.getUserById(uid)
}