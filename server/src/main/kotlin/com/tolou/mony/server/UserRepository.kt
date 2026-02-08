package com.tolou.mony.server

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepository {
    suspend fun fetchProfile(userId: Int): UserProfileResponse {
        return newSuspendedTransaction(Dispatchers.IO) {
            val user = UsersTable.selectAll().where { UsersTable.id eq userId }.single()
            UserProfileResponse(
                id = user[UsersTable.id],
                username = user[UsersTable.username]
            )
        }
    }

    suspend fun updateUsername(userId: Int, username: String): UserProfileResponse {
        return newSuspendedTransaction(Dispatchers.IO) {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.username] = username
            }
            val user = UsersTable.selectAll().where { UsersTable.id eq userId }.single()
            UserProfileResponse(
                id = user[UsersTable.id],
                username = user[UsersTable.username]
            )
        }
    }
}
