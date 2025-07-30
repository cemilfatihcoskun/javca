package com.sstek.javca.main.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sstek.javca.auth.domain.usecase.GetCurrentUserUseCase
import com.sstek.javca.auth.domain.usecase.LogOutUseCase
import com.sstek.javca.user.domain.entity.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logOutUseCase: LogOutUseCase,
) : ViewModel() {
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            _currentUser.postValue(user)
        }
    }


    fun logOut() {
        viewModelScope.launch {
            logOutUseCase()
            _currentUser.value = null
        }
    }

}