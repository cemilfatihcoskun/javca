package com.sstek.javca.presentation.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sstek.javca.domain.usecase.RegisterWithUsernameAndEmailAndPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerWithUsernameAndEmailAndPasswordUseCase: RegisterWithUsernameAndEmailAndPasswordUseCase
) : ViewModel() {
    private val _state = MutableLiveData(RegisterUiState())
    val state: LiveData<RegisterUiState> = _state

    fun register(username: String, email: String, password: String) {
        Log.e("RegisterViewModel", "register() $username, $email, $password")
        viewModelScope.launch {
            val user = registerWithUsernameAndEmailAndPasswordUseCase(username, email, password)
            if (user != null) {

                _state.value = RegisterUiState(isSuccess = true)
            } else {
                _state.value = RegisterUiState(isSuccess = false, errorMessage = "Registration failed.")
            }
        }
    }
}