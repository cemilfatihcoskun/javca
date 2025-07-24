package com.sstek.javca.auth.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.sstek.javca.auth.domain.usecase.LogInWithEmailAndPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val loginWithEmailAndPasswordUseCase: LogInWithEmailAndPasswordUseCase
) : ViewModel() {
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _loginSuccess = MutableLiveData<Boolean>(false)
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                loginWithEmailAndPasswordUseCase(email, password)
                _loginSuccess.value = true
            } catch (e: FirebaseAuthInvalidUserException) {
                _errorMessage.value = "Böyle bir kullanıcı bulunamadı."
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _errorMessage.value = "Yanlış e-posta veya şifre girdiniz."
            } catch (e: IOException) {
                _errorMessage.value = "İnternet bağlantınızı kontrol edin."
            } catch (e: Exception) {
                _errorMessage.value = "Bilinmeyen bir hata oluştu."
            }
        }
    }
}