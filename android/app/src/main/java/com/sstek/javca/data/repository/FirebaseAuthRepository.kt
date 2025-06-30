package com.sstek.javca.data.repository

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.sstek.javca.data.model.CallRequestDto
import com.sstek.javca.domain.model.CallRequest
import com.sstek.javca.domain.model.User
import com.sstek.javca.domain.repository.AuthRepository
import com.sstek.javca.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : AuthRepository {
    override suspend fun loginWithEmailAndPassword(email: String, password: String): User? {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Log.d("FirebaseAuthRepository", "loginWithWemailAndPassword() user $email, logged in successfully.")
            toUser(result.user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "loginWithEmailAndPassword() ${e.message}")
            null
        }
    }

    override suspend fun registerWithUsernameAndEmailAndPassword(
        username: String,
        email: String,
        password: String
    ): User? {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
            )?.await()

            Log.d("FirebaseAuthRepository", "registerWithUsernameAndEmailAndPassword() user $username, registered successfully.")

            //TODO(toUser kullan)
            val user = User(
                uid = result?.user?.uid.toString(),
                username = username,
                email = email
            )

            if (user != null) {
                val userRef = firebaseDatabase.getReference("users").child(user.uid)
                userRef.setValue(user).await()
            }

            user
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "registerWithEmailAndPassword() ${e.message}")
            null
        }
    }

    override fun getCurrentUser(): User? {
        return toUser(firebaseAuth.currentUser)
    }

    override suspend fun reloadAuth(): Boolean {
        return try {
            firebaseAuth.currentUser?.reload()?.await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "reloadAuth() ${e.message}")
            false
        }
    }

    override fun logOut(): Unit {
        try {
            val username = getCurrentUser()?.username
            firebaseAuth.signOut()
            Log.d("FirebaseAuthRepository", "logOut() user $username logged out successfully.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "logOut() error ${e.message}")
        }
    }
}

fun FirebaseAuthRepository.toUser(firebaseUser: FirebaseUser?): User? {
    return User(
        uid = firebaseUser?.uid.toString(),
        username = firebaseUser?.displayName.toString(),
        email = firebaseUser?.email.toString()
    )
}