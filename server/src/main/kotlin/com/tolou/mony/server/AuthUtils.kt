package com.tolou.mony.server

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

fun ApplicationCall.principalUserId(): Int {
    val principal = this.principal<JWTPrincipal>()
    return requireNotNull(principal?.payload?.getClaim("userId")?.asInt()) {
        "Missing userId claim."
    }
}
