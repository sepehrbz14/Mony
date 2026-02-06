package com.tolou.mony.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.expenseRoutes() {
    val repository = ExpenseRepository()

    authenticate("auth-jwt") {
        get("/expenses") {
            val userId = call.principalUserId()
            val expenses = repository.listExpenses(userId)
            call.respond(expenses)
        }

        post("/expenses") {
            val userId = call.principalUserId()
            val request = call.receive<ExpenseRequest>()
            if (request.title.isBlank() || request.amount <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Title and amount are required.")
                )
                return@post
            }
            val created = repository.createExpense(userId, request)
            call.respond(HttpStatusCode.Created, created)
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.principalUserId(): Int {
    val principal = this.principal<JWTPrincipal>()
    return requireNotNull(principal?.payload?.getClaim("userId")?.asInt()) {
        "Missing userId claim."
    }
}
