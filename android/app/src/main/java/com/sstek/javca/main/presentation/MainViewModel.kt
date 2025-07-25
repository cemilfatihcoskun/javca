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
import com.sstek.javca.user.domain.usecase.AddFavoriteUserUseCase
import com.sstek.javca.user.domain.usecase.RemoveFavoriteUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val sendCallRequestUseCase: SendCallRequestUseCase,
    private val reloadAuthUseCase: ReloadAuthUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val addFavoriteUserUseCase: AddFavoriteUserUseCase,
    private val removeFavoriteUserUseCase: RemoveFavoriteUserUseCase
) : ViewModel() {
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>> = _userList

    private val currentCallId: String? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            _currentUser.postValue(user)
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val userId = user?.uid
            getAllUsersUseCase { users ->
                _userList.postValue(users)
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            logOutUseCase()
            _currentUser.value = null
        }
    }

    fun startCall(callerId: String, calleeId: String, onCallStarted: (String) -> Unit) {
        if (_currentUser.value == null) return

        // DONE TODO(Sunucu zamanını kullan)
        val callRequest = CallRequest(
            callerId = _currentUser.value?.uid.toString(),
            calleeId = calleeId,
            timestamp = System.currentTimeMillis(),
            status = CallStatus.PENDING
        )

        viewModelScope.launch {
            val (callId, status) = sendCallRequestUseCase(callRequest)
            onCallStarted(callId.toString())
        }
    }

    fun toggleFavorite(userIdToToggle: String) {
        toggleFavoriteLocally(userIdToToggle)
        viewModelScope.launch(Dispatchers.IO) {
            toggleFavoriteRemotely(userIdToToggle)
        }
    }

    fun toggleFavoriteLocally(targetUserId: String) {
        val current = _currentUser.value ?: return
        val currentFavorites = current.favorites.toMutableMap()

        if (currentFavorites.containsKey(targetUserId)) {
            currentFavorites.remove(targetUserId)
        } else {
            currentFavorites[targetUserId] = true
        }

        val updatedUser = current.copy(favorites = currentFavorites)
        _currentUser.value = updatedUser
    }

    suspend fun toggleFavoriteRemotely(targetUserId: String) {
        if (_currentUser.value?.favorites?.containsKey(targetUserId) == true) {
            addFavoriteUserUseCase(_currentUser.value?.uid.toString(), targetUserId)
        } else {
            removeFavoriteUserUseCase(_currentUser.value?.uid.toString(), targetUserId)
        }
    }
}