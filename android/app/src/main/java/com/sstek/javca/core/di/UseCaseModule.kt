package com.sstek.javca.core.di

import com.sstek.javca.auth.domain.repository.AuthRepository
import com.sstek.javca.auth.domain.usecase.LogInWithEmailAndPasswordUseCase
import com.sstek.javca.auth.domain.usecase.ReloadAuthUseCase
import com.sstek.javca.user.domain.repository.UserRepository
import com.sstek.javca.user.domain.usecase.GetUserByIdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideLoginWithEmailAndPasswordUseCase(repository: AuthRepository): LogInWithEmailAndPasswordUseCase =
        LogInWithEmailAndPasswordUseCase(repository)

    @Provides
    fun provideGetUserByIdUseCase(userRepository: UserRepository): GetUserByIdUseCase = GetUserByIdUseCase(userRepository)

    @Provides
    fun provideReloadAuthUseCase(authRepository: AuthRepository): ReloadAuthUseCase = ReloadAuthUseCase(authRepository)
}
