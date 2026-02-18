package com.tolou.mony.server

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val phone: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String
)

@Serializable
data class SignupChallengeRequest(
    val phone: String,
    val password: String
)

@Serializable
data class SignupChallengeResponse(
    val challengeId: String,
    val expiresAt: String,
    val remainingAttempts: Int
)

@Serializable
data class VerifySignupChallengeRequest(
    val challengeId: String,
    val code: String
)

@Serializable
data class AuthResponse(
    val token: String
)
