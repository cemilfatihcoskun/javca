package com.sstek.javca.domain.repository

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.sstek.javca.domain.model.CallRequest

interface CallObserverRepository {
    fun observeIncomingCalls(userId: String, onCallReceived: (callId: String, callRequest: CallRequest) -> Unit)

    fun listenToCallDetails(callId: String, onCallReceived: (callId: String, callRequest: CallRequest) -> Unit)

    fun removeCallDetailsListener(callId: String)
    fun removeListener(userId: String)
}