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

    private var sentCode: String? = null

    fun sendCode(phone: String) {
        viewModelScope.launch {
            if (phone == TEST_PHONE_NUMBER) {
                state = LoginState.LoggedIn
                return@launch
            }
            state = LoginState.SendingCode
            try {
                val code = generateOtpCode()
                repository.sendOtp(phone, code)
                sentCode = code
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

    fun verifyCode(code: String) {
        viewModelScope.launch {
            state = LoginState.Verifying
            val expected = sentCode
            if (expected != null && expected == code) {
                state = LoginState.LoggedIn
            } else {
                state = LoginState.Error("Invalid code")
            }
        }
    }

    fun consumeLoggedIn() {
        if (state is LoginState.LoggedIn) {
            state = LoginState.EnterPhone
        }
    }

    private fun generateOtpCode(): String {
        return (10000..99999).random().toString()
    }

    companion object {
        const val TEST_PHONE_NUMBER = "09123456789"
    }
}
