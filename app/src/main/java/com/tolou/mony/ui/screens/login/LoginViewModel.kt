package com.tolou.mony.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolou.mony.ui.data.AuthRepository
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
                state = LoginState.Error(e.localizedMessage ?: "Failed to send code")
            }
        }
    }

    fun consumeCodeSent() {
        if (state is LoginState.CodeSent) {
            state = LoginState.EnterPhone
        }
    }

    fun verifyCode(phone: String, code: String) {
        viewModelScope.launch {
            state = LoginState.Verifying
            try {
                repository.verifyOtp(phone, code)
                state = LoginState.LoggedIn
            } catch (e: Exception) {
                state = LoginState.Error(e.localizedMessage ?: "Invalid code")
            }
        }
    }

    fun consumeLoggedIn() {
        if (state is LoginState.LoggedIn) {
            state = LoginState.EnterPhone
        }
    }
}
