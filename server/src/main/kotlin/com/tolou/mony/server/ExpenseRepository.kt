package com.tolou.mony.server

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class ExpenseRepository {
    suspend fun createExpense(userId: Int, request: ExpenseRequest): ExpenseResponse {
        return newSuspendedTransaction(Dispatchers.IO) {
            val createdAtInstant = runCatching {
                request.createdAt?.let(Instant::parse)
            }.getOrNull() ?: Instant.now()
            val id = ExpensesTable.insert {
                it[ExpensesTable.userId] = userId
                it[title] = request.title
                it[amount] = request.amount
                it[ExpensesTable.createdAt] = createdAtInstant
            } get ExpensesTable.id

            ExpenseResponse.fromEntity(id, userId, request.title, request.amount, createdAtInstant)
        }
    }

    suspend fun listExpenses(userId: Int): List<ExpenseResponse> {
        return newSuspendedTransaction(Dispatchers.IO) {
            ExpensesTable.selectAll()
                .where { ExpensesTable.userId eq userId }
                .orderBy(ExpensesTable.id, SortOrder.DESC)
                .map { row -> row.toExpenseResponse() }
        }
    }

    suspend fun deleteExpense(userId: Int, expenseId: Int): Boolean {
        return newSuspendedTransaction(Dispatchers.IO) {
            ExpensesTable.deleteWhere {
                (ExpensesTable.id eq expenseId) and (ExpensesTable.userId eq userId)
            } > 0
        }
    }

    private fun ResultRow.toExpenseResponse(): ExpenseResponse {
        return ExpenseResponse.fromEntity(
            id = this[ExpensesTable.id],
            userId = this[ExpensesTable.userId],
            title = this[ExpensesTable.title],
            amount = this[ExpensesTable.amount],
            createdAt = this[ExpensesTable.createdAt]
        )
    }
}
