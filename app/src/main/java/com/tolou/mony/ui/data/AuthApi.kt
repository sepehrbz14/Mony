package com.tolou.mony.ui.data

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class OtpParameter(
    val name: String,
    val value: String
)

data class SendOtpRequest(
    val mobile: String,
    val templateId: Int,
    val parameters: List<OtpParameter>
)

data class SendOtpResponse(
    val status: Int,
    val message: String,
    val data: OtpResponseData?
)

data class OtpResponseData(
    val messageId: Long,
    val cost: Double
)

interface AuthApi {

    @Headers("x-api-key: wl9LoHhuF758JhPwVq1YehXlynuUx89asxLsFeFUNxiUvgnW")
    @POST("send/verify")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): SendOtpResponse
}
