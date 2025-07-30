package com.sstek.javca.call.presentation


import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sstek.javca.call.domain.entity.CallStatus
import com.sstek.javca.webrtc_signalling.domain.entity.IceCandidateData
import com.sstek.javca.webrtc_signalling.domain.entity.SdpOffer
import com.sstek.javca.webrtc_signalling.domain.entity.SdpType
import com.sstek.javca.call.domain.usecase.ObserveCallRequestUseCase
import com.sstek.javca.webrtc_signalling.domain.usecase.ObserveRemoteIceUseCase
import com.sstek.javca.webrtc_signalling.domain.usecase.ObserveRemoteSdpUseCase
import com.sstek.javca.webrtc_signalling.domain.usecase.SendIceCandidateUseCase
import com.sstek.javca.webrtc_signalling.domain.usecase.SendSdpUseCase
import com.sstek.javca.call.domain.usecase.UpdateCallRequestUseCase
import com.sstek.javca.framework.WebRtcManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val sendSdpUseCase: SendSdpUseCase,
    private val sendIceCandidateUseCase: SendIceCandidateUseCase,
    private val observeRemoteSdpUseCase: ObserveRemoteSdpUseCase,
    private val observeRemoteIceUseCase: ObserveRemoteIceUseCase,
    private val updateCallStatusUseCase: UpdateCallRequestUseCase,
    private val observeCallRequestUseCase: ObserveCallRequestUseCase
) : ViewModel() {

    private val _onRemoteSdp = MutableSharedFlow<SdpOffer>()
    val onRemoteSdp: SharedFlow<SdpOffer> = _onRemoteSdp

    private val _onRemoteIce = MutableSharedFlow<IceCandidateData>()
    val onRemoteIce: SharedFlow<IceCandidateData> = _onRemoteIce

    private val _callStatus = MutableLiveData<CallStatus>()
    val callStatus: MutableLiveData<CallStatus> = _callStatus

    private lateinit var webRtcManager: WebRtcManager

    private var isCameraEnabledByUser: Boolean = true

    fun initWebRtcManager(callId: String, context: Context, localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        Log.d("CallViewModel", "initWebRtcManager() called with callId: $callId")
        webRtcManager = WebRtcManager(
            context,
            callId,
            onIceCandidate = { candidate ->
                viewModelScope.launch {
                    val candidateData = candidate.toIceCandidateData()
                    sendIceCandidateUseCase(callId, candidateData)
                }
            },
            onSdpCreated = { sdp ->
                viewModelScope.launch {
                    sendSdpUseCase(
                        callId,
                        SdpOffer(
                            type = sdp.type.toSdpType(),
                            sdp = sdp.description
                        )
                    )
                }
            },
            onCallEnded = {
                _callStatus.postValue(CallStatus.ENDED)
            }
        )
        webRtcManager.init(localView, remoteView)


        viewModelScope.launch {
            observeRemoteSdpUseCase(callId) { sdpOffer ->
                val sdp = SessionDescription(SessionDescription.Type.fromCanonicalForm(sdpOffer.type.toString()), sdpOffer.sdp)
                webRtcManager.setRemoteDescription(sdp) {
                    if (sdp.type == SessionDescription.Type.OFFER) {
                        webRtcManager.createAnswer()
                    }
                }
            }
        }


        viewModelScope.launch {
            observeRemoteIceUseCase(callId) { candidateData ->
                val iceCandidate = candidateData.toIceCandidate()
                webRtcManager.addRemoteIceCandidate(iceCandidate)
            }
        }

        viewModelScope.launch {
            observeCallRequestUseCase(callId) { callId2, request ->
                Log.d("CallViewModel", "CallRequest updated: $request")

                _callStatus.value = request.status

                if (request.status == CallStatus.ENDED) {
                    endCall(callId)
                }

            }
        }
    }

    fun createOffer() {
        webRtcManager.createOffer()
    }

    fun createAnswer() {
        webRtcManager.createAnswer()
    }

    fun endCall(callId: String) {
        cleanWebRtc()
        changeCallStatus(callId, CallStatus.ENDED)
    }

    fun changeCallStatus(callId: String, callStatus: CallStatus) {
        GlobalScope.launch {
            updateCallStatusUseCase(callId, callStatus)
        }
    }

    fun cleanWebRtc() {
        webRtcManager.close()
    }

    fun rejectCall(callId: String) {
        viewModelScope.launch {
            updateCallStatusUseCase(callId, CallStatus.REJECTED)
        }
    }

    fun acceptCall(callId: String) {
        Log.d("CallViewModel", "acceptCall() called with callId: $callId")
        createAnswer()
        viewModelScope.launch {
            updateCallStatusUseCase(callId, CallStatus.ACCEPTED)
        }
    }

    fun toggleMicrophone(): Boolean {
        return webRtcManager.toggleMicrophone()
    }

    fun toggleVideo(): Boolean {
        isCameraEnabledByUser = !isCameraEnabledByUser
        return webRtcManager.toggleVideo()
    }

    fun getVideoStatus(): Boolean {
        return webRtcManager.getVideoStatus()
    }

    fun switchCamera() {
        webRtcManager.switchCamera()
    }

    fun startCameraIfItIsEnabledByUser() {
        Log.d("CallViewModel" , "startCameraIfItIsEnabledByUser() $isCameraEnabledByUser")
        if (isCameraEnabledByUser) {
            webRtcManager.startCamera()
        }
    }

    fun stopCamera() {
        Log.d("CallViewModel", "stopCamera() $isCameraEnabledByUser")
        webRtcManager.stopCamera()
    }

    fun swapSurfaceViewRenderers() {
        webRtcManager.swapSurfaceViewRenderers()
    }
}

fun IceCandidate.toIceCandidateData() = IceCandidateData(
    sdpMid = this.sdpMid ?: "",
    sdpMLineIndex = this.sdpMLineIndex,
    candidate = this.sdp
)

fun IceCandidateData.toIceCandidate() = IceCandidate(
    this.sdpMid,
    this.sdpMLineIndex,
    this.candidate
)

fun SdpType.toSessionDescriptionType(): SessionDescription.Type = when (this) {
    SdpType.OFFER -> SessionDescription.Type.OFFER
    SdpType.ANSWER -> SessionDescription.Type.ANSWER
    SdpType.PRANSWER -> SessionDescription.Type.PRANSWER
    SdpType.ROLLBACK -> SessionDescription.Type.ROLLBACK
}

fun SessionDescription.Type.toSdpType(): SdpType = when (this) {
    SessionDescription.Type.OFFER -> SdpType.OFFER
    SessionDescription.Type.ANSWER -> SdpType.ANSWER
    SessionDescription.Type.PRANSWER -> SdpType.PRANSWER
    SessionDescription.Type.ROLLBACK -> SdpType.ROLLBACK
    else -> throw IllegalArgumentException("Unsupported SDP type")
}

