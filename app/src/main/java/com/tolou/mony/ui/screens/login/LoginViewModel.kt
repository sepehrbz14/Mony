package com.tolou.mony.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolou.mony.ui.data.AuthRepository
import com.tolou.mony.ui.data.SmsRepository
import kotlinx.coroutines.launch


sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object CodeSent : LoginState()
    object LoggedIn : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val smsRepository: SmsRepository
) : ViewModel() {

    var state by mutableStateOf<LoginState>(LoginState.Idle)
        private set

    private var pendingPhone: String? = null
    private var pendingPassword: String? = null
    private var pendingOtp: String? = null

    fun sendSignupCode(phone: String, password: String) {
        if (phone.isBlank() || password.isBlank()) {
            state = LoginState.Error("Phone and password are required.")
            return
        }
        viewModelScope.launch {
            state = LoginState.Loading
            try {
                val code = generateOtpCode()
                smsRepository.sendOtp(phone, code)
                pendingPhone = phone
                pendingPassword = password
                pendingOtp = code
                state = LoginState.CodeSent
            } catch (e: Exception) {
                state = LoginState.Error(e.localizedMessage ?: "Failed to send OTP")
            }
        }
    }

    fun verifySignupCode(code: String) {
        viewModelScope.launch {
            state = LoginState.Loading
            val expected = pendingOtp
            val phone = pendingPhone
            val password = pendingPassword
            if (expected == null || phone == null || password == null) {
                state = LoginState.Error("Missing signup data. Please try again.")
                return@launch
            }
            if (expected != code) {
                state = LoginState.Error("Invalid code")
                return@launch
            }
            try {
                authRepository.register(phone, password)
                clearPendingSignup()
                state = LoginState.LoggedIn
            } catch (e: Exception) {
                state = LoginState.Error(e.localizedMessage ?: "Registration failed")
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
                authRepository.login(phone, password)
                state = LoginState.LoggedIn
            } catch (e: Exception) {
                state = LoginState.Error(e.localizedMessage ?: "Login failed")
            }
        }
    }

    fun consumeLoggedIn() {
        if (state is LoginState.LoggedIn) {
            state = LoginState.Idle
        }
    }

    fun consumeCodeSent() {
        if (state is LoginState.CodeSent) {
            state = LoginState.Idle
        }
    }

    private fun clearPendingSignup() {
        pendingPhone = null
        pendingPassword = null
        pendingOtp = null
    }

    private fun generateOtpCode(): String {
        return (10000..99999).random().toString()
    }
}
