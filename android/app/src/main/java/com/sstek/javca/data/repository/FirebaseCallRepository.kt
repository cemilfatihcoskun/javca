package com.sstek.javca.data.repository

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.sstek.javca.Config
import com.sstek.javca.domain.model.CallRequest
import com.sstek.javca.domain.model.CallStatus
import com.sstek.javca.domain.repository.CallRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseCallRepository @Inject constructor(
    private val database: FirebaseDatabase
) : CallRepository {

    // TODO(Edit callstatuses')
    override suspend fun sendCallRequest(callRequest: CallRequest): CallStatus {
        return try {
            val callRef = database.getReference("calls").push()
            val callId = callRef.key
            val callData = callRequest.copy()

            callRef.setValue(callData).await()
            database.getReference("user/calls/${callRequest.callerId}/$callId").setValue(true)
            database.getReference("user/calls/${callRequest.calleeId}/$callId").setValue(true)

            delay(Config.TIMEOUT_MILLISECONDS)
            val statusSnapshot = callRef.child("status").get().await()
            val status = statusSnapshot.getValue(String::class.java)

            if (status == "PENDING") {
                callRef.child("status").setValue("TIMEOUT").await()
                return CallStatus.TIMEOUT
            }
            return CallStatus.ACCEPTED
        } catch (e: Exception) {
            Log.e("FirebaseCallRepository", "sendCallRequest() error ${e.message}")
            CallStatus.PENDING
        }
    }

    override suspend fun updateCallStatus(callId: String, status: CallStatus) {
        try {
            val ref = database.getReference("calls/$callId/status")
            ref.setValue(status.name).await()
        } catch (e: Exception) {
            Log.e("FirebaseCallRepo", "updateCallStatus error: ${e.message}")
        }
    }

    override suspend fun observeCall(calleeId: String, onCallReceived: (CallRequest) -> Unit) {
        val ref = database.getReference("calls")
        ref.orderByChild("calleeId").equalTo(calleeId).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseCallRepo", "observeCall() onCancelled()")
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("FirebaseCallRepo", "observeCall() onChildAdded()")
                val callRequest = snapshot.getValue(CallRequest::class.java)
                if (callRequest != null) {
                    onCallReceived(callRequest)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("FirebaseCallRepo", "observeCall() onChildChanged()")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("FirebaseCallRepo", "observeCall() onChildMoved()")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("FirebaseCallRepo", "observeCall() onChildRemoved()")
            }
        })
    }

}