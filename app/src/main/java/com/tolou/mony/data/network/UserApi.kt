package com.tolou.mony.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

data class UserProfileRequest(
    val username: String
)

data class UserProfileResponse(
    val id: Int,
    val username: String?
)

interface UserApi {
    @GET("profile")
    suspend fun fetchProfile(
        @Header("Authorization") token: String
    ): UserProfileResponse

    @PUT("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UserProfileRequest
    ): UserProfileResponse
}
