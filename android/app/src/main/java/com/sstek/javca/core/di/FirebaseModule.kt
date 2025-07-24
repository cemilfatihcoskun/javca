package com.sstek.javca.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sstek.javca.core.config.Config
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("tr")
        auth.useEmulator(Config.FIREBASE_HOST_IP, Config.FIREBASE_AUTHENTICATION_PORT)
        return auth
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val db = FirebaseDatabase.getInstance()
        db.useEmulator(Config.FIREBASE_HOST_IP, Config.FIREBASE_DATABASE_PORT)
        return db
    }
}
