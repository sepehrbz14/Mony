package com.tolou.mony.ui.data

import com.tolou.mony.data.network.UserApi
import com.tolou.mony.data.network.ChangePasswordRequest
import com.tolou.mony.data.network.ChangePasswordResponse
import com.tolou.mony.data.network.UserProfileRequest
import com.tolou.mony.data.network.UserProfileResponse

class UserRepository(
    private val api: UserApi,
    private val authRepository: AuthRepository
) {
    suspend fun fetchProfile(): UserProfileResponse {
        val token = requireNotNull(authRepository.token()) { "Missing auth token." }
        return api.fetchProfile("Bearer $token")
    }

    suspend fun updateUsername(username: String): UserProfileResponse {
        val token = requireNotNull(authRepository.token()) { "Missing auth token." }
        return api.updateProfile(
            token = "Bearer $token",
            request = UserProfileRequest(username = username)
        )
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): ChangePasswordResponse {
        val token = requireNotNull(authRepository.token()) { "Missing auth token." }
        return api.changePassword(
            token = "Bearer $token",
            request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        )
    }
}
