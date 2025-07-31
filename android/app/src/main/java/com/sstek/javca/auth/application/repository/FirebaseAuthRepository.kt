package com.sstek.javca.auth.application.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
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

    private var presenceListener: ValueEventListener? = null

    override suspend fun loginWithEmailAndPassword(email: String, password: String): User? {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        Log.d("FirebaseAuthRepository", "loginWithWemailAndPassword() user $email, logged in successfully.")

        //setupPresence()

        return getCurrentUser()
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

        //TODO(toUser kullan, serverTime kullan)
        val user = User(
            uid = result?.user?.uid.toString(),
            username = username,
            email = email,
            status = "online",
            lastSeen = System.currentTimeMillis()
        )
        if (user != null) {
            val userRef = firebaseDatabase.getReference("users").child(user.uid)
            userRef.setValue(user).await()
        }

        //setupPresence()

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

    override suspend fun reloadAuth(): User? {
        val user = firebaseAuth.currentUser ?: return null

        return try {
            user.reload().await()

            val userSnapshot = firebaseDatabase
                .getReference("users")
                .child(user.uid)
                .get()
                .await()

            if (!userSnapshot.exists()) {
                Log.d("FirebaseAuthRepository", "User data not found in database")
                return null
            }

            val userFromDb = userSnapshot.getValue(User::class.java)

            if (userFromDb?.uid.isNullOrBlank() || userFromDb?.username.isNullOrBlank()) {
                Log.d("FirebaseAuthRepository", "User data incomplete: $userFromDb")
                return null
            }

            return userFromDb
        } catch (e: Exception) {
            Log.d("FirebaseAuthRepository", "reloadAuth error: ${e.message}")
            null
        }
    }


    override suspend fun logOut() {
        try {
            firebaseAuth.currentUser?.let {
                val userStatusRef = firebaseDatabase.getReference("users")
                    .child(it.uid)
                val offlineStatus = mapOf(
                    "status" to "offline",
                    "lastSeen" to ServerValue.TIMESTAMP
                )
                userStatusRef.updateChildren(offlineStatus)
            }

            firebaseAuth.signOut()

            presenceListener?.let {
                val connectedRef = firebaseDatabase.getReference(".info/connected")
                connectedRef.removeEventListener(it)
                presenceListener = null
            }

            Log.d("FirebaseAuthRepository", "logOut()")
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "logOut() error ${e.message}")
        }
    }

    override fun setupPresence() {
        val currentUser = firebaseAuth.currentUser ?: return

        val userStatusRef = firebaseDatabase.getReference("users")
            .child(currentUser.uid)
        val connectedRef = firebaseDatabase.getReference(".info/connected")

        presenceListener?.let {
            connectedRef.removeEventListener(it)
        }

        presenceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    val onlineStatus = mapOf("status" to "online")
                    val offlineStatus = mapOf(
                        "status" to "offline",
                        "lastSeen" to ServerValue.TIMESTAMP
                    )
                    userStatusRef.onDisconnect().updateChildren(offlineStatus)
                    userStatusRef.updateChildren(onlineStatus)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseAuthRepository", "Listener cancelled", error.toException())
            }
        }

        connectedRef.addValueEventListener(presenceListener!!)
    }


}

fun FirebaseAuthRepository.toUser(firebaseUser: FirebaseUser?): User? {
    return User(
        uid = firebaseUser?.uid.toString(),
        username = firebaseUser?.displayName.toString() ?: "",
        email = firebaseUser?.email.toString() ?: ""
    )
}