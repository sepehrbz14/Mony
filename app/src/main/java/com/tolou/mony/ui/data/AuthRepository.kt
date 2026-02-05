package com.tolou.mony.ui.data


class AuthRepository(
    private val api: AuthApi
) {

    suspend fun sendOtp(phone: String) {
        api.sendOtp(SendOtpRequest(phone))
    }

    suspend fun verifyOtp(phone: String, code: String): String {
        return api.verifyOtp(
            VerifyOtpRequest(phone, code)
        ).token
    }
}
