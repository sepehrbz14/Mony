package com.tolou.mony.server

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class ExpenseRepository {
    suspend fun createExpense(request: ExpenseRequest): ExpenseResponse {
        return newSuspendedTransaction(Dispatchers.IO) {
            val now = Instant.now()
            val id = ExpensesTable.insert {
                it[title] = request.title
                it[amount] = request.amount
                it[createdAt] = now
            } get ExpensesTable.id

            ExpenseResponse.fromEntity(id, request.title, request.amount, now)
        }
    }

    suspend fun listExpenses(): List<ExpenseResponse> {
        return newSuspendedTransaction(Dispatchers.IO) {
            ExpensesTable.selectAll()
                .orderBy(ExpensesTable.id, SortOrder.DESC)
                .map { row -> row.toExpenseResponse() }
        }
    }

    private fun ResultRow.toExpenseResponse(): ExpenseResponse {
        return ExpenseResponse.fromEntity(
            id = this[ExpensesTable.id],
            title = this[ExpensesTable.title],
            amount = this[ExpensesTable.amount],
            createdAt = this[ExpensesTable.createdAt]
        )
    }
}
