package com.sstek.javca.launcher.presentation




import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sstek.javca.auth.domain.usecase.ReloadAuthUseCase
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