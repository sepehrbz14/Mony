package com.tolou.mony.server

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class IncomeRepository {
    suspend fun createIncome(userId: Int, request: IncomeRequest): IncomeResponse {
        return newSuspendedTransaction(Dispatchers.IO) {
            val now = Instant.now()
            val id = IncomesTable.insert {
                it[IncomesTable.userId] = userId
                it[IncomesTable.title] = request.title
                it[IncomesTable.amount] = request.amount
                it[IncomesTable.createdAt] = now
            } get IncomesTable.id
            IncomeResponse.fromEntity(id, userId, request.title, request.amount, now)
        }
    }

    suspend fun listIncomes(userId: Int): List<IncomeResponse> {
        return newSuspendedTransaction(Dispatchers.IO) {
            IncomesTable.selectAll()
                .where { IncomesTable.userId eq userId }
                .orderBy(IncomesTable.id, SortOrder.DESC)
                .map { row ->
                    IncomeResponse.fromEntity(
                        id = row[IncomesTable.id],
                        userId = row[IncomesTable.userId],
                        title = row[IncomesTable.title],
                        amount = row[IncomesTable.amount],
                        createdAt = row[IncomesTable.createdAt]
                    )
                }
        }
    }

    suspend fun deleteIncome(userId: Int, incomeId: Int): Boolean {
        return newSuspendedTransaction(Dispatchers.IO) {
            IncomesTable.deleteWhere {
                (IncomesTable.id eq incomeId) and (IncomesTable.userId eq userId)
            } > 0
        }
    }
}
