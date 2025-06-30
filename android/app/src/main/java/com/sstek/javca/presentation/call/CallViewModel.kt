package com.sstek.javca.presentation.call

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sstek.javca.domain.usecase.LogInWithEmailAndPasswordUseCase
import com.sstek.javca.domain.usecase.UpdateCallRequestUseCase
import com.sstek.javca.presentation.login.LogInUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

package com.sstek.javca.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sstek.javca.domain.model.CallStatus
import com.sstek.javca.domain.model.IceCandidateData
import com.sstek.javca.domain.model.SdpOffer
import com.sstek.javca.domain.repository.CallRepository
import com.sstek.javca.domain.usecase.ObserveRemoteIceUseCase
import com.sstek.javca.domain.usecase.ObserveRemoteSdpUseCase
import com.sstek.javca.domain.usecase.SendIceCandidateUseCase
import com.sstek.javca.domain.usecase.SendSdpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val sendSdpUseCase: SendSdpUseCase,
    private val sendIceCandidateUseCase: SendIceCandidateUseCase,
    private val observeRemoteSdpUseCase: ObserveRemoteSdpUseCase,
    private val observeRemoteIceUseCase: ObserveRemoteIceUseCase,
    private val updateCallStatusUseCase: UpdateCallRequestUseCase,
    private val callRepository: CallRepository
) : ViewModel() {

    private val _onRemoteSdp = MutableSharedFlow<SdpOffer>()
    val onRemoteSdp: SharedFlow<SdpOffer> = _onRemoteSdp

    private val _onRemoteIce = MutableSharedFlow<IceCandidateData>()
    val onRemoteIce: SharedFlow<IceCandidateData> = _onRemoteIce

    fun sendSdp(callId: String, sdp: SdpOffer) {
        viewModelScope.launch {
            sendSdpUseCase(callId, sdp)
        }
    }

    fun sendIceCandidate(callId: String, candidate: IceCandidateData) {
        viewModelScope.launch {
            sendIceCandidateUseCase(callId, candidate)
        }
    }

    suspend fun observeRemoteSdp(callId: String) {
        observeRemoteSdpUseCase(callId) { sdp ->
            viewModelScope.launch {
                _onRemoteSdp.emit(sdp)
            }
        }
    }

    suspend fun observeRemoteIceCandidates(callId: String) {
        observeRemoteIceUseCase(callId) { candidate ->
            viewModelScope.launch {
                _onRemoteIce.emit(candidate)
            }
        }
    }

    fun updateCallStatus(callId: String, status: CallStatus) {
        viewModelScope.launch {
            updateCallStatusUseCase(callId, status)
        }
    }

    fun endCall(callId: String) {
        updateCallStatus(callId, CallStatus.ENDED)
    }

    fun rejectCall(callId: String) {
        updateCallStatus(callId, CallStatus.REJECTED)
    }
}
