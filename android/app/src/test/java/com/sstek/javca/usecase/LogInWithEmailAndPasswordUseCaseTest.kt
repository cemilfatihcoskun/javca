package com.sstek.javca.usecase

import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.auth.domain.repository.AuthRepository
import com.sstek.javca.auth.domain.usecase.LogInWithEmailAndPasswordUseCase
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock

// given when then
class LogInWithEmailAndPasswordUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var logInUseCase: LogInWithEmailAndPasswordUseCase

    private val email = "harezmi@gmail.com"
    private val password = "000000"

    @Before
    fun setup() {
        authRepository = mock()
        logInUseCase = LogInWithEmailAndPasswordUseCase(authRepository)
    }

    @Test
    fun `given credentials are valid then return user`() = runBlocking {
        val mockUser: User = mock()
        `when`(authRepository.loginWithEmailAndPassword(email, password))
            .thenReturn(mockUser)

        val result = logInUseCase(email, password)

        assertNotNull(result)
        assertEquals(mockUser, result)
    }

    @Test
    fun `given credentials are invalid then return null`() = runBlocking {
        `when`(authRepository.loginWithEmailAndPassword(email, password))
            .thenReturn(null)

        val result = logInUseCase(email, password)

        assertNull(result)
    }
}