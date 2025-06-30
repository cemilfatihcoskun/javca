package com.sstek.javca.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sstek.javca.domain.model.CallRequest
import com.sstek.javca.domain.model.CallStatus
import com.sstek.javca.domain.model.User
import com.sstek.javca.domain.usecase.GetAllUsersUseCase
import com.sstek.javca.domain.usecase.GetCurrentUserUseCase
import com.sstek.javca.domain.usecase.LogOutUseCase
import com.sstek.javca.domain.usecase.ReloadAuthUseCase
import com.sstek.javca.domain.usecase.SendCallRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val sendCallRequestUseCase: SendCallRequestUseCase,
    private val reloadAuthUseCase: ReloadAuthUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase
) : ViewModel() {
    private val _username = MutableLiveData<String?>()
    val username: LiveData<String?> = _username

    private val _callState = MutableLiveData<CallUiState>()
    val callState: LiveData<CallUiState> get() = _callState

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>> = _userList

    fun loadUsers() {
        viewModelScope.launch {
            val users = getAllUsersUseCase()
            _userList.value = users
        }
    }

    private val currentCallId: String? = null

    fun checkUser() {
        val user = getCurrentUserUseCase()
        _username.value = user?.username
    }

    fun getCurrentUser(): User? {
        return getCurrentUserUseCase()
    }

    fun logOut() {
        logOutUseCase()
        _username.value = null
    }

    fun reloadAuth() {
        viewModelScope.launch {
            reloadAuthUseCase()
        }
    }


    fun startCall(callerId: String, calleeId: String) {
        val caller: User? = getCurrentUserUseCase()

        if (caller == null) {
            return
        }

        val callRequest = CallRequest(
            callerId = caller.uid,
            calleeId = calleeId,
        )

        viewModelScope.launch {
            _callState.value = CallUiState.Loading
            val success = sendCallRequestUseCase(callRequest)

            if (success == CallStatus.TIMEOUT) {
                _callState.value = CallUiState.Error("Aradığın kişi şu anda telefona cevap vermiyor. İstersen sonra dene.")
            }

        }
    }
}

sealed class CallUiState {
    object Idle: CallUiState()
    object Loading: CallUiState()
    data class Success(val message: String): CallUiState()
    data class Error(val message: String): CallUiState()
}