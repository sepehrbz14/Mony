package com.tolou.mony.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put

fun Route.userRoutes() {
    val repository = UserRepository()

    authenticate("auth-jwt") {
        get("/profile") {
            val userId = call.principalUserId()
            val profile = repository.fetchProfile(userId)
            call.respond(profile)
        }

        put("/profile") {
            val userId = call.principalUserId()
            val request = call.receive<UserProfileRequest>()
            val trimmed = request.username.trim()
            if (trimmed.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Username is required.")
                )
                return@put
            }
            val updated = repository.updateUsername(userId, trimmed)
            call.respond(updated)
        }
    }
}
