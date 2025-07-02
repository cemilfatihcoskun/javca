package com.sstek.javca.usecase

import com.google.firebase.auth.FirebaseUser
import com.sstek.javca.domain.model.User
import com.sstek.javca.domain.repository.AuthRepository
import com.sstek.javca.domain.repository.UserRepository
import com.sstek.javca.domain.usecase.GetAllUsersUseCase
import com.sstek.javca.domain.usecase.LogInWithEmailAndPasswordUseCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
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