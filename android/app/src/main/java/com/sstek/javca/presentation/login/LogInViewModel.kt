package com.sstek.javca.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sstek.javca.domain.usecase.LogInWithEmailAndPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val loginWithEmailAndPasswordUseCase: LogInWithEmailAndPasswordUseCase
) : ViewModel() {
    private val _state = MutableLiveData(LogInUiState())
    val state: LiveData<LogInUiState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = loginWithEmailAndPasswordUseCase(email, password)
            if (user != null) {
                _state.value = LogInUiState(isSuccess = true)
            } else {
                _state.value = LogInUiState(isSuccess = false, errorMessage = "Login failed.")
            }
        }
    }
}