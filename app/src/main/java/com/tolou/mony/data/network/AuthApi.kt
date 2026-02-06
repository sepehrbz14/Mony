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

interface AuthApi {

    @POST("auth/register")
    suspend fun register(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): AuthResponse
}
