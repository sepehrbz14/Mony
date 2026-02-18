package com.tolou.mony.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

interface OtpSender {
    suspend fun sendOtp(phone: String, code: String)
}

class SmsIrOtpSender(
    private val apiKey: String,
    private val templateId: Int,
    private val baseUrl: String = "https://api.sms.ir/v1/",
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
) : OtpSender {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun sendOtp(phone: String, code: String) {
        if (apiKey.isBlank()) {
            throw IOException("SMS API key is not configured")
        }

        val payload = SendOtpRequest(
            mobile = phone,
            templateId = templateId,
            parameters = listOf(
                OtpParameter(
                    name = "CODE",
                    value = code
                )
            )
        )

        val request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl.trimEnd('/') + "/send/verify"))
            .timeout(Duration.ofSeconds(15))
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(json.encodeToString(payload)))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw IOException("SMS provider returned ${response.statusCode()}")
        }

        val parsed = runCatching { json.decodeFromString<SendOtpResponse>(response.body()) }.getOrNull()
        if (parsed != null && parsed.status !in 1..2) {
            throw IOException(parsed.message.ifBlank { "SMS provider rejected request" })
        }
    }
}

@Serializable
private data class OtpParameter(
    val name: String,
    val value: String
)

@Serializable
private data class SendOtpRequest(
    val mobile: String,
    val templateId: Int,
    val parameters: List<OtpParameter>
)

@Serializable
private data class SendOtpResponse(
    val status: Int = 0,
    val message: String = ""
)
