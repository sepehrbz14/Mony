package com.tolou.mony.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolou.mony.ui.data.AuthRepository
import com.tolou.mony.ui.utils.toUserMessage
import kotlinx.coroutines.launch
import java.time.Instant

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class OtpSent(
        val phone: String,
        val challengeId: String,
        val expiresAt: String,
        val remainingAttempts: Int
    ) : LoginState()

    data class LoggedIn(val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var state: LoginState by mutableStateOf(LoginState.Idle)
        private set

    private var pendingChallengeId: String? = null
    var otpVerifyErrorMessage: String? by mutableStateOf(null)
        private set

    fun sendSignupCode(phone: String, password: String) {
        if (phone.isBlank() || password.isBlank()) {
            state = LoginState.Error("Phone and password are required.")
            return
        }
        viewModelScope.launch {
            state = LoginState.Loading
            otpVerifyErrorMessage = null
            try {
                val challenge = repository.requestSignupChallenge(phone, password)
                pendingChallengeId = challenge.challengeId
                state = LoginState.OtpSent(
                    phone = phone,
                    challengeId = challenge.challengeId,
                    expiresAt = challenge.expiresAt,
                    remainingAttempts = challenge.remainingAttempts
                )
            } catch (e: Exception) {
                state = LoginState.Error(e.toUserMessage("Failed to send OTP"))
            }
        }
    }

    fun verifySignup(code: String) {
        viewModelScope.launch {
            val otpStateSnapshot = state as? LoginState.OtpSent
            val challengeId = pendingChallengeId
            if (challengeId.isNullOrBlank()) {
                state = LoginState.Error("Missing signup challenge. Please request a new code.")
                return@launch
            }
            if (code.isBlank()) {
                otpVerifyErrorMessage = "Please enter the OTP code."
                return@launch
            }

            state = LoginState.Loading
            otpVerifyErrorMessage = null
            try {
                val token = repository.verifySignupChallenge(challengeId, code.trim())
                pendingChallengeId = null
                otpVerifyErrorMessage = null
                state = LoginState.LoggedIn(token)
            } catch (e: Exception) {
                otpVerifyErrorMessage = e.toUserMessage("Invalid or expired signup code")
                state = otpStateSnapshot ?: LoginState.Error(otpVerifyErrorMessage ?: "Invalid or expired signup code")
            }
        }
    }

    fun consumeLoggedIn() {
        if (state is LoginState.LoggedIn) {
            state = LoginState.Idle
        }
    }

    fun consumeCodeSent() {
        // Keep OtpSent state so Verify screen can read challenge metadata.
    }

    fun verifySignupCode(code: String) {
        verifySignup(code)
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

    fun otpExpiresAtEpochSeconds(): Long? {
        val otpState = state as? LoginState.OtpSent ?: return null
        return runCatching { Instant.parse(otpState.expiresAt).epochSecond }.getOrNull()
    }
}
