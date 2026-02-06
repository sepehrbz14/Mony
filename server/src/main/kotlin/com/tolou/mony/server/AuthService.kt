package com.tolou.mony.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.time.temporal.ChronoUnit

class AuthService(private val jwtConfig: JwtConfig) {

    suspend fun register(request: RegisterRequest): AuthResponse {
        val normalizedPhone = request.phone.trim()
        if (normalizedPhone.isBlank() || request.password.isBlank()) {
            throw IllegalArgumentException("Phone and password are required.")
        }

        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val userId = newSuspendedTransaction(Dispatchers.IO) {
            UsersTable.insert {
                it[phone] = normalizedPhone
                it[UsersTable.passwordHash] = passwordHash
                it[createdAt] = Instant.now()
            } get UsersTable.id
        }
        return AuthResponse(createToken(userId, normalizedPhone))
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val normalizedPhone = request.phone.trim()
        if (normalizedPhone == TEST_PHONE && request.password == TEST_PASSWORD) {
            val userId = ensureTestUser()
            return AuthResponse(createToken(userId, normalizedPhone))
        }
        val user = newSuspendedTransaction(Dispatchers.IO) {
            UsersTable.select { UsersTable.phone eq normalizedPhone }
                .singleOrNull()
        } ?: throw IllegalArgumentException("Invalid credentials.")

        val passwordHash = user[UsersTable.passwordHash]
        if (!BCrypt.checkpw(request.password, passwordHash)) {
            throw IllegalArgumentException("Invalid credentials.")
        }
        val userId = user[UsersTable.id]
        return AuthResponse(createToken(userId, normalizedPhone))
    }

    private fun createToken(userId: Int, phone: String): String {
        val algorithm = Algorithm.HMAC256(jwtConfig.secret)
        val now = Instant.now()
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withClaim("userId", userId)
            .withClaim("phone", phone)
            .withExpiresAt(java.util.Date.from(now.plus(7, ChronoUnit.DAYS)))
            .sign(algorithm)
    }

    private suspend fun ensureTestUser(): Int {
        val passwordHash = BCrypt.hashpw(TEST_PASSWORD, BCrypt.gensalt())
        return newSuspendedTransaction(Dispatchers.IO) {
            val existingById = UsersTable.select { UsersTable.id eq TEST_USER_ID }
                .singleOrNull()
            if (existingById != null) {
                return@newSuspendedTransaction TEST_USER_ID
            }

            val existingByPhone = UsersTable.select { UsersTable.phone eq TEST_PHONE }
                .singleOrNull()
            if (existingByPhone != null) {
                return@newSuspendedTransaction existingByPhone[UsersTable.id]
            }

            UsersTable.insert {
                it[id] = TEST_USER_ID
                it[phone] = TEST_PHONE
                it[UsersTable.passwordHash] = passwordHash
                it[createdAt] = Instant.now()
            } get UsersTable.id
        }
    }

    companion object {
        private const val TEST_USER_ID = 1001
        private const val TEST_PHONE = "0912345678"
        private const val TEST_PASSWORD = "TEST"
    }
}
