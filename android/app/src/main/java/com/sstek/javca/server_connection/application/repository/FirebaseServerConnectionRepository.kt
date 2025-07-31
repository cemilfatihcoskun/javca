package com.sstek.javca.server_connection.application.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sstek.javca.server_connection.domain.repository.ServerConnectionRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseServerConnectionRepository @Inject constructor(
    private val database: FirebaseDatabase
) : ServerConnectionRepository {

    override fun observeConnectionStatus(callback: (Boolean) -> Unit) {
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                callback(connected)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    // TOOO(Düzenle bunu)
    override suspend fun isServerConnected(): Boolean {
        return true

        return try {
            val snapshot = database.getReference(".info/connected")
                .get()
                .await()

            snapshot.getValue(Boolean::class.java) == true
        } catch (e: Exception) {
            false
        }
    }
}
