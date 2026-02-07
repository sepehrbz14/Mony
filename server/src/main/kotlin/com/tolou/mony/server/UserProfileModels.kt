package com.tolou.mony.server

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileRequest(
    val username: String
)

@Serializable
data class UserProfileResponse(
    val id: Int,
    val username: String?
)
