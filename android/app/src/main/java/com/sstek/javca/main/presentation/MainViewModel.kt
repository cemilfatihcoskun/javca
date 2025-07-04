package com.sstek.javca.main.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sstek.javca.call.domain.entity.CallRequest
import com.sstek.javca.call.domain.entity.CallStatus
import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.user.domain.usecase.GetAllUsersUseCase
import com.sstek.javca.auth.domain.usecase.GetCurrentUserUseCase
import com.sstek.javca.auth.domain.usecase.LogOutUseCase
import com.sstek.javca.auth.domain.usecase.ReloadAuthUseCase
import com.sstek.javca.call.domain.usecase.SendCallRequestUseCase
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

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>> = _userList

    private val currentCallId: String? = null

    fun loadUsers() {
        viewModelScope.launch {
            getAllUsersUseCase { users ->
                Log.d("MainViewModel", "loadUsers called")
                _userList.postValue(users)
            }
        }
    }

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

    fun startCall(callerId: String, calleeId: String, onCallStarted: (String) -> Unit) {
        val caller: User? = getCurrentUserUseCase()

        if (caller == null) return

        // DONE TODO(Sunucu zamanını kullan)
        val callRequest = CallRequest(
            callerId = caller.uid,
            calleeId = calleeId,
            timestamp = System.currentTimeMillis(),
            status = CallStatus.PENDING
        )

        viewModelScope.launch {
            val (callId, status) = sendCallRequestUseCase(callRequest)
            onCallStarted(callId.toString())
        }
    }
}