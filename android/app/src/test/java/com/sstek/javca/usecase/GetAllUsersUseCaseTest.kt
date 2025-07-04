package com.sstek.javca.usecase

import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.user.domain.repository.UserRepository
import com.sstek.javca.user.domain.usecase.GetAllUsersUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetAllUsersUseCaseTest {
    private lateinit var userRepository: UserRepository
    private lateinit var getAllUsersUseCase: GetAllUsersUseCase

    @Before
    fun setup() {
        userRepository = mock()
        getAllUsersUseCase = GetAllUsersUseCase(userRepository)
    }

    @Test
    fun `given call getAllUsers then return users`() = runBlocking {
        val sampleUsers = listOf(
            User(uid = "1", username = "Alice"),
            User(uid = "2", username = "Bob")
        )

        whenever(userRepository.getAllUsers(org.mockito.kotlin.any())).thenAnswer { invocation ->
            val callback = invocation.getArgument<(List<User>) -> Unit>(0)
            callback(sampleUsers)
        }

        var receivedUsers: List<User>? = null
        getAllUsersUseCase.invoke { users ->
            receivedUsers = users
        }

        assert(receivedUsers != null)
        assert(receivedUsers!!.size == 2)
        assert(receivedUsers!![0].username == "Alice")
        assert(receivedUsers!![1].username == "Bob")

        verify(userRepository).getAllUsers(org.mockito.kotlin.any())
    }
}