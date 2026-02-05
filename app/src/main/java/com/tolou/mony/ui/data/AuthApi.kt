package com.tolou.mony.ui.data

import retrofit2.http.Body
import retrofit2.http.POST

data class SendOtpRequest(val phone: String)
data class VerifyOtpRequest(val phone: String, val code: String)
data class TokenResponse(val token: String)

interface AuthApi {

    @POST("auth/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    )

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): TokenResponse
}