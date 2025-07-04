package com.sstek.javca.core.di

import com.google.firebase.database.FirebaseDatabase
import com.sstek.javca.call.application.repository.FirebaseCallObserverRepository
import com.sstek.javca.call.application.repository.FirebaseCallRepository
import com.sstek.javca.call.domain.repository.CallObserverRepository
import com.sstek.javca.call.domain.repository.CallRepository
import com.sstek.javca.user.application.repository.FirebaseUserRepository
import com.sstek.javca.user.domain.repository.UserRepository
import com.sstek.javca.webrtc_signalling.application.repository.FirebaseSignalingRepository
import com.sstek.javca.webrtc_signalling.domain.repository.SignalingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideCallRepository(db: FirebaseDatabase): CallRepository =
        FirebaseCallRepository(db)

    @Provides
    @Singleton
    fun provideCallObserverRepository(db: FirebaseDatabase): CallObserverRepository =
        FirebaseCallObserverRepository(db)

    @Provides
    @Singleton
    fun provideUserRepository(db: FirebaseDatabase): UserRepository =
        FirebaseUserRepository(db)

    @Provides
    @Singleton
    fun provideSignalingRepository(db: FirebaseDatabase): SignalingRepository =
        FirebaseSignalingRepository(db)
}
