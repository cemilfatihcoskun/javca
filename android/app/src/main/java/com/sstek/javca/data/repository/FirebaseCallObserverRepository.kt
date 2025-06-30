package com.sstek.javca.data.repository

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sstek.javca.domain.model.CallRequest
import com.sstek.javca.domain.repository.CallObserverRepository
import javax.inject.Inject

class FirebaseCallObserverRepository @Inject constructor(
    private val database: FirebaseDatabase
) : CallObserverRepository {

    private val callDetailListeners = mutableMapOf<String, ValueEventListener>()

    override fun observeIncomingCalls(userId: String, onCallReceived: (callId: String, callRequest: CallRequest) -> Unit) {
        val userCallsRef = database.getReference("user/calls/$userId")

        userCallsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val callId = snapshot.key ?: return
                listenToCallDetails(callId, onCallReceived)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val callId = snapshot.key ?: return
                removeCallDetailsListener(callId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseCallObserverRepo", "userCalls listener cancelled: ${error.message}")
            }
        })
    }

    override fun listenToCallDetails(callId: String, onCallReceived: (callId: String, callRequest: CallRequest) -> Unit) {
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
        Log.d("FirebaseCallObserverRepo", "Removed call details listener for $callId")
    }

    override fun removeListener(userId: String) {
        val userCallsRef = database.getReference("user/calls/$userId")
        // userCallsRef.removeEventListener(...) // Eğer ChildEventListener referansın varsa
        callDetailListeners.forEach { (callId, listener) ->
            val callRef = database.getReference("calls/$callId")
            callRef.removeEventListener(listener)
        }
        callDetailListeners.clear()
        Log.d("FirebaseCallObserverRepo", "All listeners removed")
    }
}

