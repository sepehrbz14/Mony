package com.tolou.mony.ui.data

import com.tolou.mony.data.network.OtpParameter
import com.tolou.mony.data.network.SendOtpRequest
import com.tolou.mony.data.network.SmsApi

class SmsRepository(
    private val api: SmsApi
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
