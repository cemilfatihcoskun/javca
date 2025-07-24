package com.sstek.javca.call.application.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.sstek.javca.call.domain.entity.CallRequest
import com.sstek.javca.call.domain.entity.CallStatus
import com.sstek.javca.call.domain.repository.CallRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseCallRepository @Inject constructor(
    private val database: FirebaseDatabase
) : CallRepository {

    // TODO(Edit callstatuses')
    override suspend fun sendCallRequest(callRequest: CallRequest): Pair<String?, CallStatus> {
        return try {
            val callRef = database.getReference("calls").push()
            val callId = callRef.key
            val callData = callRequest.copy()

            callRef.setValue(callData).await()
            database.getReference("userCalls/${callRequest.callerId}/$callId").setValue(true).await()
            database.getReference("userCalls/${callRequest.calleeId}/$callId").setValue(true).await()


            val statusSnapshot = callRef.child("status").get().await()
            val status = statusSnapshot.getValue(String::class.java)

            return when (status) {
                "PENDING" -> {
                    //callRef.child("status").setValue("TIMEOUT").await()
                    Pair(callId, CallStatus.TIMEOUT)
                }
                "REJECTED" -> Pair(callId, CallStatus.REJECTED)
                "ACCEPTED" -> Pair(callId, CallStatus.ACCEPTED)
                "TIMEOUT" -> Pair(callId, CallStatus.TIMEOUT)
                else -> Pair(callId, CallStatus.PENDING)
            }
        } catch (e: Exception) {
            Log.e("FirebaseCallRepository", "sendCallRequest() error ${e.message}")
            Pair(null, CallStatus.PENDING)
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

}