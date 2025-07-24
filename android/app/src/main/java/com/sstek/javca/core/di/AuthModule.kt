package com.sstek.javca.core.di

import com.sstek.javca.auth.application.repository.FirebaseAuthRepository
import com.sstek.javca.auth.domain.repository.AuthRepository
import com.sstek.javca.core.provider.CurrentUserProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCurrentUserProvider(
        impl: FirebaseAuthRepository
    ): CurrentUserProvider
}
