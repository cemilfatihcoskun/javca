package com.sstek.javca.call_history.application.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sstek.javca.call.domain.entity.Call
import com.sstek.javca.call_history.domain.repository.CallHistoryRepository
import com.sstek.javca.call_history.domain.repository.ListenerHandle
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseCallHistoryRepositoryRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) : CallHistoryRepository {

    override suspend fun getCallHistoryForUser(userId: String): List<Call> = suspendCoroutine { cont ->
        val database = firebaseDatabase.getReference("userCalls")
            .child(userId)
            .limitToLast(50)
            .get()
            .addOnSuccessListener { snapshot ->

                val callIds = snapshot.children.mapNotNull { it.key }.reversed()

                if (callIds.isEmpty()) {
                    cont.resume(emptyList())
                    return@addOnSuccessListener
                }

                val calls = mutableListOf<Call>()
                var counter = 0
                var failed = false

                callIds.forEach { callId ->
                    firebaseDatabase.getReference("calls").child(callId).get().addOnSuccessListener { callSnap ->
                        callSnap.getValue(Call::class.java)?.let { calls.add(it) }
                        counter++
                        if (counter == callIds.size && !failed) cont.resume(calls)
                    }.addOnFailureListener {
                        if (!failed) {
                            failed = true
                            cont.resumeWithException(it)
                        }
                    }
                }
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    override fun observeCallHistoryForUser(
        userId: String,
        onUpdated: (List<Call>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerHandle {
        val ref = firebaseDatabase.getReference("userCalls").child(userId).limitToLast(50)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val callIds = snapshot.children.mapNotNull { it.key }.reversed()

                if (callIds.isEmpty()) {
                    onUpdated(emptyList())
                    return
                }

                val calls = mutableListOf<Call>()
                var counter = 0
                var failed = false

                callIds.forEach { callId ->
                    firebaseDatabase.getReference("calls").child(callId).get()
                        .addOnSuccessListener { callSnap ->
                            callSnap.getValue(Call::class.java)?.let { calls.add(it) }
                            counter++
                            if (counter == callIds.size && !failed) onUpdated(calls)
                        }
                        .addOnFailureListener {
                            if (!failed) {
                                failed = true
                                onError(it)
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }
        }

        ref.addValueEventListener(listener)

        return object : ListenerHandle {
            override fun remove() {
                ref.removeEventListener(listener)
            }
        }
    }

}