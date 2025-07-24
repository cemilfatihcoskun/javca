package com.sstek.javca.auth.application.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.auth.domain.repository.AuthRepository
import com.sstek.javca.core.provider.CurrentUserProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// TODO(firebaseDatabase i artık kaldır ve onun yerine cloud function çalışsın)
// Pek mantıklı değil doğrudan burada yazmak daha basit
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : AuthRepository, CurrentUserProvider {

    override suspend fun loginWithEmailAndPassword(email: String, password: String): User? {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        Log.d("FirebaseAuthRepository", "loginWithWemailAndPassword() user $email, logged in successfully.")
        return toUser(result.user)
    }

    override suspend fun registerWithUsernameAndEmailAndPassword(
        username: String,
        email: String,
        password: String
    ): User? {
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

        return user
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        val uid = firebaseUser.uid

        val userRef = firebaseDatabase.getReference("users").child(uid)
        return try {
            val snapshot = userRef.get().await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Error fetching user data from database: ${e.message}")
            null
        }
    }

    suspend override fun reloadAuth(): User? {
        val user = firebaseAuth.currentUser
        return if (user != null) {
            try {
                user.reload().await()
                toUser(firebaseAuth.currentUser)
            } catch (e: Exception) {
                Log.d("FirebaseAuthRepository", "${e.message}")
                null
            }
        } else {
            null
        }
    }

    override suspend fun logOut() {
        try {
            firebaseAuth.signOut()
            Log.d("FirebaseAuthRepository", "logOut()")
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "logOut() error ${e.message}")
        }
    }

}

fun FirebaseAuthRepository.toUser(firebaseUser: FirebaseUser?): User? {
    return User(
        uid = firebaseUser?.uid.toString(),
        username = firebaseUser?.displayName.toString() ?: "",
        email = firebaseUser?.email.toString() ?: ""
    )
}