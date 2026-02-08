package com.tolou.mony.server

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object IncomesTable : Table("incomes") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id)
    val title = text("title")
    val amount = long("amount")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class IncomeRequest(
    val title: String,
    val amount: Long
)

@Serializable
data class IncomeResponse(
    val id: Int,
    val userId: Int,
    val title: String,
    val amount: Long,
    val createdAt: String
) {
    companion object {
        fun fromEntity(
            id: Int,
            userId: Int,
            title: String,
            amount: Long,
            createdAt: Instant
        ): IncomeResponse {
            return IncomeResponse(
                id = id,
                userId = userId,
                title = title,
                amount = amount,
                createdAt = createdAt.toString()
            )
        }
    }
}
