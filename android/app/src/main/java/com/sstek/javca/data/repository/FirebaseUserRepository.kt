package com.sstek.javca.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.sstek.javca.domain.model.User
import com.sstek.javca.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseUserRepository @Inject constructor(
    val database: FirebaseDatabase
) : UserRepository {

    //TODO(Listenerlı sürekli dinleyecek şekilde yap)
    override suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = database.getReference("users").get().await()

            val users = mutableListOf<User>()
            for (userSnapshot in snapshot.children) {
                val uid = userSnapshot.child("uid").getValue(String::class.java)
                val username = userSnapshot.child("username").getValue(String::class.java)
                val email = userSnapshot.child("email").getValue(String::class.java)

                if (uid != null && username != null && email != null) {
                    val user = User(uid = uid, username = username, email = email)
                    users.add(user)
                    Log.d("FirebaseUserRepo", "getAllUsers() $user")
                }
            }
            users
        } catch (e: Exception) {
            Log.d("FirebaseUserRepo", "getAllUsers() ${e.message}")
            emptyList()
        }
    }

    override suspend fun getUserById(uid: String): User? = suspendCoroutine { cont ->
        val ref = database.getReference("users").child(uid)
        ref.get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            cont.resume(user)
        }.addOnFailureListener {
            cont.resume(null)
        }
    }
}