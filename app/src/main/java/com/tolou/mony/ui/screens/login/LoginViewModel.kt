package com.tolou.mony.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolou.mony.ui.data.AuthApi
import com.tolou.mony.ui.data.AuthRepository
import com.tolou.mony.ui.data.SendOtpRequest
import com.tolou.mony.ui.data.VerifyOtpRequest
import kotlinx.coroutines.launch


sealed class LoginState {
    object EnterPhone : LoginState()
    object SendingCode : LoginState()
    data class CodeSent(val phone: String) : LoginState()
    object Verifying : LoginState()
    object LoggedIn : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf<LoginState>(LoginState.EnterPhone)
        private set

    fun sendCode(phone: String) {
        viewModelScope.launch {
            state = LoginState.SendingCode
            try {
                repository.sendOtp(phone)
                state = LoginState.CodeSent(phone)
            } catch (e: Exception) {
                state = LoginState.Error("Failed to send code")
            }
        }
    }

    fun verifyCode(phone: String, code: String) {
        viewModelScope.launch {
            state = LoginState.Verifying
            try {
                repository.verifyOtp(phone, code)
                state = LoginState.LoggedIn
            } catch (e: Exception) {
                state = LoginState.Error("Invalid code")
            }
        }
    }
}


