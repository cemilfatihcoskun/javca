package com.sstek.javca.webrtc_signalling.application.repository

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sstek.javca.webrtc_signalling.domain.entity.IceCandidateData
import com.sstek.javca.webrtc_signalling.domain.entity.SdpOffer
import com.sstek.javca.webrtc_signalling.domain.repository.SignalingRepository
import javax.inject.Inject

class FirebaseSignalingRepository @Inject constructor(
    private val database: FirebaseDatabase
) : SignalingRepository {

    override suspend fun sendSdp(callId: String, sdp: SdpOffer) {
        database.getReference("webrtc/$callId/sdp").setValue(sdp)
    }

    override suspend fun sendIceCandidate(callId: String, candidate: IceCandidateData) {
        database.getReference("webrtc/$callId/iceCandidates").push().setValue(candidate)
    }

    override fun observeRemoteSdp(callId: String, onReceived: (SdpOffer) -> Unit) {
        Log.d("FirebaseSignalingRepo", "webrtc/$callId/sdp")
        database.getReference("webrtc/$callId/sdp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(SdpOffer::class.java)?.let(onReceived)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseSignalingRepo", "observeRemoteSdp() ${error.message}")
            }
        })
    }

    override fun observeRemoteIceCandidates(callId: String, onReceived: (IceCandidateData) -> Unit) {
        database.getReference("webrtc/$callId/iceCandidates").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(IceCandidateData::class.java)?.let(onReceived)
                Log.d("FirebaseSignalingRepo", "observeRemoteIceCandidates() onChildAdded()")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseSignalingRepo", "observeRemoteIceCandidates() onCancelled()")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("FirebaseSignalingRepo", "observeRemoteIceCandidates() onChildChanged()")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("FirebaseSignalingRepo", "observeRemoteIceCandidates() onChildMoved()")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("FirebaseSignalingRepo", "observeRemoteIceCandidates() onChildRemoved()")
            }
        })
    }

    override fun clearSignalingData(callId: String) {
        database.getReference("webrtc/$callId").removeValue()
    }
}
