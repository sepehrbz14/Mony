package com.tolou.mony.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.expenseRoutes() {
    val repository = ExpenseRepository()

    get("/expenses") {
        val expenses = repository.listExpenses()
        call.respond(expenses)
    }

    post("/expenses") {
        val request = call.receive<ExpenseRequest>()
        if (request.title.isBlank() || request.amount <= 0) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Title and amount are required.")
            )
            return@post
        }
        val created = repository.createExpense(request)
        call.respond(HttpStatusCode.Created, created)
    }
}
