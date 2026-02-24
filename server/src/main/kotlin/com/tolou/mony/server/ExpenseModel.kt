package com.tolou.mony.server

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object ExpensesTable : Table("expenses") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id)
    val title = text("title")
    val amount = long("amount")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val phone = text("phone").uniqueIndex()
    val passwordHash = text("password_hash")
    val username = text("username").nullable()
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class ExpenseRequest(
    val title: String,
    val amount: Long,
    val createdAt: String? = null
)

@Serializable
data class ExpenseResponse(
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
        ): ExpenseResponse {
            return ExpenseResponse(
                id = id,
                userId = userId,
                title = title,
                amount = amount,
                createdAt = createdAt.toString()
            )
        }
    }
}
