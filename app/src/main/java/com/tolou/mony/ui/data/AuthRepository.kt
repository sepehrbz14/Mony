package com.tolou.mony.ui.data


class AuthRepository(
    private val api: AuthApi
) {
    private val templateId = 123456

    suspend fun sendOtp(phone: String, code: String) {
        api.sendOtp(
            SendOtpRequest(
                mobile = phone,
                templateId = templateId,
                parameters = listOf(
                    OtpParameter(
                        name = "Code",
                        value = code
                    )
                )
            )
        )
    }
}
