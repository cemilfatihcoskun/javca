package com.sstek.javca.launcher.presentation




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
class LauncherViewModel @Inject constructor(
    private val reloadAuthUseCase: ReloadAuthUseCase
) : ViewModel() {

    fun reloadAuth(onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            if (reloadAuthUseCase() != null) {
                onSuccess()
            } else {
                onFailure()
            }
        }
    }
}