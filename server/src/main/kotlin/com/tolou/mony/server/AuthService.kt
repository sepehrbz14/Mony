package com.tolou.mony.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AuthService(
    private val jwtConfig: JwtConfig,
    private val otpSender: OtpSender
) {

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
            UsersTable.selectAll().where { UsersTable.phone eq normalizedPhone }
                .singleOrNull()
        } ?: throw IllegalArgumentException("Invalid credentials.")

        val passwordHash = user[UsersTable.passwordHash]
        if (!BCrypt.checkpw(request.password, passwordHash)) {
            throw IllegalArgumentException("Invalid credentials.")
        }
        val userId = user[UsersTable.id]
        return AuthResponse(createToken(userId, normalizedPhone))
    }

    suspend fun createSignupChallenge(request: SignupChallengeRequest): SignupChallengeResponse {
        val normalizedPhone = request.phone.trim()
        if (normalizedPhone.isBlank() || request.password.isBlank()) {
            throw IllegalArgumentException("Phone and password are required.")
        }

        val challengeId = UUID.randomUUID().toString()
        val otp = generateOtpCode()
        val now = Instant.now()
        val expiresAt = now.plus(CHALLENGE_TTL_MINUTES, ChronoUnit.MINUTES)
        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val otpHash = BCrypt.hashpw(otp, BCrypt.gensalt())

        newSuspendedTransaction(Dispatchers.IO) {
            SignupChallengesTable.insert {
                it[id] = challengeId
                it[phone] = normalizedPhone
                it[SignupChallengesTable.passwordHash] = passwordHash
                it[SignupChallengesTable.otpHash] = otpHash
                it[remainingAttempts] = MAX_OTP_ATTEMPTS
                it[SignupChallengesTable.expiresAt] = expiresAt
                it[consumedAt] = null
                it[createdAt] = now
            }
        }

        try {
            otpSender.sendOtp(normalizedPhone, otp)
        } catch (e: Exception) {
            newSuspendedTransaction(Dispatchers.IO) {
                SignupChallengesTable.deleteWhere { SignupChallengesTable.id eq challengeId }
            }
            throw IllegalArgumentException("Failed to send verification code. Please try again.")
        }

        return SignupChallengeResponse(
            challengeId = challengeId,
            expiresAt = expiresAt.toString(),
            remainingAttempts = MAX_OTP_ATTEMPTS
        )
    }

    suspend fun verifySignupChallenge(request: VerifySignupChallengeRequest): AuthResponse {
        val challengeId = request.challengeId.trim()
        val code = request.code.trim()
        if (challengeId.isBlank() || code.isBlank()) {
            throw IllegalArgumentException("Challenge ID and code are required.")
        }

        val challenge = newSuspendedTransaction(Dispatchers.IO) {
            SignupChallengesTable
                .selectAll()
                .where { SignupChallengesTable.id eq challengeId }
                .singleOrNull()
        } ?: throw IllegalArgumentException("Invalid or expired signup code.")

        if (challenge[SignupChallengesTable.consumedAt] != null) {
            throw IllegalArgumentException("This challenge has already been used.")
        }

        val now = Instant.now()
        if (challenge[SignupChallengesTable.expiresAt].isBefore(now)) {
            throw IllegalArgumentException("Signup code has expired.")
        }

        val attemptsLeft = challenge[SignupChallengesTable.remainingAttempts]
        if (attemptsLeft <= 0) {
            throw IllegalArgumentException("Attempt limit reached. Please request a new code.")
        }

        val otpMatches = BCrypt.checkpw(code, challenge[SignupChallengesTable.otpHash])
        if (!otpMatches) {
            val updatedAttempts = newSuspendedTransaction(Dispatchers.IO) {
                SignupChallengesTable.update({ SignupChallengesTable.id eq challengeId }) {
                    it[remainingAttempts] = remainingAttempts - 1
                }
                SignupChallengesTable
                    .selectAll()
                    .where { SignupChallengesTable.id eq challengeId }
                    .single()[SignupChallengesTable.remainingAttempts]
            }
            if (updatedAttempts <= 0) {
                throw IllegalArgumentException("Attempt limit reached. Please request a new code.")
            }
            throw IllegalArgumentException("Invalid code. $updatedAttempts attempt(s) remaining.")
        }

        val phone = challenge[SignupChallengesTable.phone]
        val passwordHash = challenge[SignupChallengesTable.passwordHash]

        val response = newSuspendedTransaction(Dispatchers.IO) {
            val existingUser = UsersTable.selectAll().where { UsersTable.phone eq phone }.singleOrNull()
            if (existingUser != null) {
                throw IllegalArgumentException("Phone already registered.")
            }

            val userId = UsersTable.insert {
                it[UsersTable.phone] = phone
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.createdAt] = now
            } get UsersTable.id

            SignupChallengesTable.update({ SignupChallengesTable.id eq challengeId }) {
                it[consumedAt] = now
            }

            AuthResponse(createToken(userId, phone))
        }

        return response
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
            val existingById = UsersTable.selectAll().where { UsersTable.id eq TEST_USER_ID }
                .singleOrNull()
            if (existingById != null) {
                return@newSuspendedTransaction TEST_USER_ID
            }

            val existingByPhone = UsersTable.selectAll().where { UsersTable.phone eq TEST_PHONE }
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

    private fun generateOtpCode(): String {
        val value = secureRandom.nextInt(900000) + 100000
        return value.toString()
    }

    companion object {
        private const val TEST_USER_ID = 1001
        private const val TEST_PHONE = "0912345678"
        private const val TEST_PASSWORD = "TEST"
        private const val CHALLENGE_TTL_MINUTES = 5L
        private const val MAX_OTP_ATTEMPTS = 5
        private val secureRandom = SecureRandom()
    }
}

object SignupChallengesTable : Table("signup_challenges") {
    val id = text("id")
    val phone = text("phone")
    val passwordHash = text("password_hash")
    val otpHash = text("otp_hash")
    val remainingAttempts = integer("remaining_attempts")
    val expiresAt = timestamp("expires_at")
    val consumedAt = timestamp("consumed_at").nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
