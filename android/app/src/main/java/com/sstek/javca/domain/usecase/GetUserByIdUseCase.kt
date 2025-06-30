package com.sstek.javca.domain.usecase

import com.sstek.javca.data.repository.FirebaseUserRepository
import com.sstek.javca.domain.repository.UserRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String) = userRepository.getUserById(uid)
}