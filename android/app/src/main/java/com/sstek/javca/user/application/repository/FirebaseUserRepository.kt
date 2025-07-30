package com.sstek.javca.user.application.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.user.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseUserRepository @Inject constructor(
    val database: FirebaseDatabase
) : UserRepository {

    //DONE TODO(Listenerlı sürekli dinleyecek şekilde yap)


    override fun getAllUsers(onUsersUpdated: (List<User>) -> Unit) {
        database.getReference("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (childSnapshot in snapshot.children) {
                    val user = childSnapshot.getValue(User::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }
                onUsersUpdated(users)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseUserRepository", "getUsersRealtime cancelled: $error")
            }
        })
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

    override suspend fun addFavoriteUser(currentUserId: String, favoriteUserId: String) {
        database.getReference("users")
            .child(currentUserId)
            .child("favorites")
            .child(favoriteUserId)
            .setValue(true)
    }

    override suspend fun removeFavoriteUser(currentUserId: String, favoriteUserId: String) {
        database.getReference("users")
            .child(currentUserId)
            .child("favorites")
            .child(favoriteUserId)
            .removeValue()
    }

    override suspend fun makeOffline(currentUserId: String) {
        database.getReference("users")
            .child(currentUserId)
            .child("status")
            .setValue("offline")
    }
}