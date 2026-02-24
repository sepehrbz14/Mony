package com.tolou.mony.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.incomeRoutes() {
    val repository = IncomeRepository()

    authenticate("auth-jwt") {
        get("/incomes") {
            val userId = call.principalUserId()
            val incomes = repository.listIncomes(userId)
            call.respond(incomes)
        }

        post("/incomes") {
            val userId = call.principalUserId()
            val request = call.receive<IncomeRequest>()
            if (request.title.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Title is required."))
                return@post
            }
            val created = repository.createIncome(userId, request)
            call.respond(HttpStatusCode.Created, created)
        }

        delete("/incomes/{id}") {
            val userId = call.principalUserId()
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid income id."))
                return@delete
            }
            val deleted = repository.deleteIncome(userId, id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Income not found."))
            }
        }
    }
}
