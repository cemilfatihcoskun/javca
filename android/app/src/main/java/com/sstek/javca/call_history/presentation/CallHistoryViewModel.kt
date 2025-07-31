package com.sstek.javca.call_history.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sstek.javca.auth.domain.usecase.GetCurrentUserUseCase
import com.sstek.javca.call.domain.entity.Call
import com.sstek.javca.call.domain.entity.CallStatus
import com.sstek.javca.call.domain.usecase.SendCallRequestUseCase
import com.sstek.javca.call_history.domain.repository.ListenerHandle
import com.sstek.javca.call_history.domain.usecase.GetCallHistoryUseCase
import com.sstek.javca.call_history.domain.usecase.ObserveCallHistoryUseCase
import com.sstek.javca.server_connection.domain.usecase.CheckServerConnectionUseCase
import com.sstek.javca.server_connection.domain.usecase.ObserveServerConnectionUseCase
import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.user.domain.usecase.GetAllUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    private val getCallHistoryUseCase: GetCallHistoryUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val sendCallRequestUseCase: SendCallRequestUseCase,
    private val observeCallHistoryUseCase: ObserveCallHistoryUseCase,
    private val checkServerConnectionUseCase: CheckServerConnectionUseCase
) : ViewModel() {

    private val _callHistory = MutableLiveData<List<Call>>()
    val callHistory: LiveData<List<Call>> = _callHistory

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _usersMap = MutableLiveData<Map<String, User>>()
    val usersMap: LiveData<Map<String, User>> = _usersMap

    fun checkServerConnectionOnce(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isConnected = checkServerConnectionUseCase()
            onResult(isConnected)
        }
    }

    fun loadCallHistory(userId: String) {
        viewModelScope.launch {
            val result = getCallHistoryUseCase(userId)
            _callHistory.value = result
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            getAllUsersUseCase { users ->
                _usersMap.value = users.associateBy { user ->
                    user.uid
                }
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            user?.let {
                _currentUser.value = it
                startObserving(it.uid)
            }

        }
    }

    fun startCall(callerId: String, calleeId: String, onCallStarted: (String?) -> Unit) {
        if (_currentUser.value == null) return

        // DONE TODO(Sunucu zamanını kullan)
        val call = Call(
            callerId = _currentUser.value?.uid.toString(),
            calleeId = calleeId,
            timestamp = 0,
            status = CallStatus.PENDING
        )

        viewModelScope.launch {
            val (callId, status) = sendCallRequestUseCase(call)
            onCallStarted(callId)
        }
    }

    private var listenerHandle: ListenerHandle? = null

    fun startObserving(userId: String) {
        listenerHandle?.remove()
        listenerHandle = observeCallHistoryUseCase(
            userId,
            onUpdated = { _callHistory.postValue(it) },
            onError = { Log.e("CallHistoryVM", "observe error", it) }
        )
    }

    override fun onCleared() {
        listenerHandle?.remove()
        super.onCleared()
    }
}
