package com.tolou.mony.data.network

import retrofit2.http.Body
import retrofit2.http.POST

data class AuthRequest(
    val phone: String,
    val password: String
)

data class AuthResponse(
    val token: String
)

data class SignupChallengeRequest(
    val phone: String,
    val password: String
)

data class SignupChallengeResponse(
    val challengeId: String,
    val expiresAt: String,
    val remainingAttempts: Int
)

data class VerifySignupChallengeRequest(
    val challengeId: String,
    val code: String
)

interface AuthApi {

    @POST("auth/register")
    suspend fun register(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("auth/signup/challenge")
    suspend fun startSignupChallenge(
        @Body request: SignupChallengeRequest
    ): SignupChallengeResponse

    @POST("auth/signup/verify")
    suspend fun verifySignupChallenge(
        @Body request: VerifySignupChallengeRequest
    ): AuthResponse
}
