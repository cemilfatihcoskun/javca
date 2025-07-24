package com.sstek.javca.call.application.repository

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sstek.javca.call.domain.entity.CallRequest
import com.sstek.javca.call.domain.repository.CallObserverRepository
import javax.inject.Inject

class FirebaseCallObserverRepository @Inject constructor(
    private val database: FirebaseDatabase
) : CallObserverRepository {

    private val callDetailListeners = mutableMapOf<String, ValueEventListener>()
    private val activeCallIds = mutableSetOf<String>()
    private val userCallListeners = mutableMapOf<String, ChildEventListener>()

    override fun observeIncomingCalls(
        userId: String,
        onCallReceived: (callId: String, callRequest: CallRequest) -> Unit
    ) {
        Log.d("FirebaseCallObRepo", "$userId")
        val userCallsRef = database.getReference("userCalls/$userId")

        if (userCallListeners.containsKey(userId)) return

        val childListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val callId = snapshot.key ?: return

                if (activeCallIds.contains(callId)) return
                activeCallIds.add(callId)

                listenToCallDetails(callId, onCallReceived)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val callId = snapshot.key ?: return
                removeCallDetailsListener(callId)
                activeCallIds.remove(callId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("CallObserverRepo", "userCalls listener cancelled: ${error.message}")
            }
        }

        userCallsRef.addChildEventListener(childListener)
        userCallListeners[userId] = childListener
    }

    override fun listenToCallDetails(
        callId: String,
        onCallReceived: (callId: String, callRequest: CallRequest) -> Unit
    ) {
        val callRef = database.getReference("calls/$callId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val call = snapshot.getValue(CallRequest::class.java) ?: return
                onCallReceived(callId, call)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseCallObserverRepo", "call listener cancelled: ${error.message}")
            }
        }

        callRef.addValueEventListener(listener)
        callDetailListeners[callId] = listener
    }

    override fun removeCallDetailsListener(callId: String) {
        val listener = callDetailListeners.remove(callId) ?: return
        val callRef = database.getReference("calls/$callId")
        callRef.removeEventListener(listener)
        activeCallIds.remove(callId)
        Log.d("FirebaseCallObserverRepo", "Removed call details listener for $callId")
    }

    override fun removeListener(userId: String) {

        val userCallsRef = database.getReference("userCalls/$userId")
        userCallListeners.remove(userId)?.let { userCallsRef.removeEventListener(it) }


        for ((callId, listener) in callDetailListeners) {
            val callRef = database.getReference("calls/$callId")
            callRef.removeEventListener(listener)
        }
        callDetailListeners.clear()
        activeCallIds.clear()

        Log.d("FirebaseCallObserverRepo", "All listeners removed for $userId")
    }
}
