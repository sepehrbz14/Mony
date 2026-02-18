package com.tolou.mony.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolou.mony.ui.data.AuthRepository
import com.tolou.mony.ui.data.SmsRepository
import com.tolou.mony.ui.utils.toUserMessage
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class OtpSent(val phone: String) : LoginState()
    data class LoggedIn(val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    private val repository: AuthRepository,
    private val smsRepository: SmsRepository
) : ViewModel() {

    var state: LoginState = LoginState.Idle
        private set

    private var pendingPhone: String? = null
    private var pendingPassword: String? = null

    fun sendSignupCode(phone: String, password: String) {
        if (phone.isBlank() || password.isBlank()) {
            state = LoginState.Error("Phone and password are required.")
            return
        }
        viewModelScope.launch {
            state = LoginState.Loading
            try {
                val code = (1000..9999).random().toString()
                smsRepository.sendOtp(phone, code)
                pendingPhone = phone
                pendingPassword = password
                state = LoginState.OtpSent(phone)
            } catch (e: Exception) {
                state = LoginState.Error(e.toUserMessage("Failed to send OTP"))
            }
        }
    }

    fun verifySignup(code: String) {
        viewModelScope.launch {
            val phone = pendingPhone
            val password = pendingPassword

            if (phone == null || password == null) {
                state = LoginState.Error("Missing signup data. Please try again.")
                return@launch
            }
            if (code.length != 4) {
                state = LoginState.Error("Invalid code")
                return@launch
            }

            state = LoginState.Loading
            try {
                val token = repository.register(phone, password)
                state = LoginState.LoggedIn(token)
            } catch (e: Exception) {
                state = LoginState.Error(e.toUserMessage("Registration failed"))
            }
        }
    }

    fun login(phone: String, password: String) {
        if (phone.isBlank() || password.isBlank()) {
            state = LoginState.Error("Phone and password are required.")
            return
        }

        viewModelScope.launch {
            state = LoginState.Loading
            try {
                val token = repository.login(phone, password)
                state = LoginState.LoggedIn(token)
            } catch (e: Exception) {
                state = LoginState.Error(e.toUserMessage("Login failed"))
            }
        }
    }
}
