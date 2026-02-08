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

        delete("/expenses/{id}") {
            val userId = call.principalUserId()
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid expense id."))
                return@delete
            }
            val deleted = repository.deleteExpense(userId, id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Expense not found."))
            }
        }
    }
}
