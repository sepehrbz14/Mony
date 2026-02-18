package com.tolou.mony.ui.data

import com.tolou.mony.data.SessionStorage
import com.tolou.mony.data.network.AuthApi
import com.tolou.mony.data.network.AuthRequest
import com.tolou.mony.data.network.SignupChallengeRequest
import com.tolou.mony.data.network.SignupChallengeResponse
import com.tolou.mony.data.network.VerifySignupChallengeRequest

class AuthRepository(
    private val api: AuthApi,
    private val sessionStorage: SessionStorage
) {
    suspend fun register(phone: String, password: String): String {
        val response = api.register(AuthRequest(phone = phone, password = password))
        sessionStorage.saveToken(response.token)
        return response.token
    }

    suspend fun login(phone: String, password: String): String {
        val response = api.login(AuthRequest(phone = phone, password = password))
        sessionStorage.saveToken(response.token)
        return response.token
    }

    suspend fun requestSignupChallenge(phone: String, password: String): SignupChallengeResponse {
        return api.startSignupChallenge(
            SignupChallengeRequest(
                phone = phone,
                password = password
            )
        )
    }

    suspend fun verifySignupChallenge(challengeId: String, code: String): String {
        val response = api.verifySignupChallenge(
            VerifySignupChallengeRequest(
                challengeId = challengeId,
                code = code
            )
        )
        sessionStorage.saveToken(response.token)
        return response.token
    }

    fun token(): String? = sessionStorage.fetchToken()

    fun clearSession() {
        sessionStorage.clear()
    }
}
