package com.sstek.javca.auth.presentation.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthWebException
import com.sstek.javca.auth.domain.domain.usecase.RegisterWithUsernameAndEmailAndPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerWithUsernameAndEmailAndPasswordUseCase: RegisterWithUsernameAndEmailAndPasswordUseCase
) : ViewModel() {
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                registerWithUsernameAndEmailAndPasswordUseCase(username, email, password)
            } catch (e: FirebaseAuthUserCollisionException) {
                _errorMessage.value = "Bu kullanıcı halihazırda var."
            } catch (e: FirebaseAuthWeakPasswordException) {
                _errorMessage.value = "Zayıf şifre. En az 6 karakterden oluşan bir şifre giriniz."
            } catch (e: FirebaseAuthWebException) {
                _errorMessage.value = "Ağ hatası."
            } catch (e: IOException) {
                _errorMessage.value = "İnternet bağlantınızı kontrol edin."
            } catch (e: Exception) {
                _errorMessage.value = "Bilinmeyen bir hata oluştu."
            }
        }
    }
}