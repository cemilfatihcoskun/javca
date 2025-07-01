package com.sstek.javca.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sstek.javca.Config
import com.sstek.javca.domain.repository.AuthRepository
import com.sstek.javca.data.repository.FirebaseAuthRepository
import com.sstek.javca.data.repository.FirebaseCallObserverRepository
import com.sstek.javca.data.repository.FirebaseCallRepository
import com.sstek.javca.data.repository.FirebaseSignalingRepository
import com.sstek.javca.data.repository.FirebaseUserRepository
import com.sstek.javca.data.source.FakeAuthDataSource
import com.sstek.javca.domain.repository.CallObserverRepository
import com.sstek.javca.domain.repository.CallRepository
import com.sstek.javca.domain.repository.SignalingRepository
import com.sstek.javca.domain.repository.UserRepository
import com.sstek.javca.domain.usecase.LogInWithEmailAndPasswordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    fun provideDataSource(): FakeAuthDataSource = FakeAuthDataSource()

    @Provides
    fun provideLoginWithEmailAndPasswordUseCase(repository: AuthRepository): LogInWithEmailAndPasswordUseCase =
        LogInWithEmailAndPasswordUseCase(repository)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("tr")
        auth.useEmulator(Config.FIREBASE_IP, Config.FIREBASE_AUTHENTICATION_PORT)
        return auth
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val db = FirebaseDatabase.getInstance()
        db.useEmulator(Config.FIREBASE_IP, Config.FIREBASE_DATABASE_PORT)
        return db
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firebaseDatabase: FirebaseDatabase
    ): AuthRepository = FirebaseAuthRepository(firebaseAuth, firebaseDatabase)



    @Provides
    @Singleton
    fun provideCallRepository(
        firebaseDatabase: FirebaseDatabase
    ): CallRepository = FirebaseCallRepository(firebaseDatabase)


    @Provides
    @Singleton
    fun provideCallObserverRepository(db: FirebaseDatabase): CallObserverRepository {
        return FirebaseCallObserverRepository(db)
    }

    @Provides
    @Singleton
    fun provideFirebaseUserRepository(db: FirebaseDatabase): UserRepository {
        return FirebaseUserRepository(db)
    }

    @Provides
    @Singleton
    fun provideFirebaseSignalingRepository(db: FirebaseDatabase): SignalingRepository {
        return FirebaseSignalingRepository(db)
    }
}