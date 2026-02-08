package com.tolou.mony.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import java.net.URI

object DatabaseFactory {
    operator fun invoke(config: ApplicationConfig) {
        val envConfig = resolveEnvironmentDatabase()
        val url = envConfig?.jdbcUrl
            ?: config.propertyOrNull("database.url")?.getString()
            ?: "jdbc:postgresql://localhost:5432/mony"
        val user = envConfig?.user
            ?: config.propertyOrNull("database.user")?.getString()
            ?: "mony"
        val password = envConfig?.password
            ?: config.propertyOrNull("database.password")?.getString()
            ?: "mony"

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            username = user
            this.password = password
            maximumPoolSize = 10
            driverClassName = "org.postgresql.Driver"
            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)
        runMigrations(dataSource)
        Database.connect(dataSource)
    }

    private fun runMigrations(dataSource: HikariDataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .validateMigrationNaming(true)
            .load()
            .migrate()
    }

    private fun resolveEnvironmentDatabase(): DbConfig? {
        val rawUrl = System.getenv("DATABASE_URL")
            ?: System.getenv("DATABASE_PRIVATE_URL")
            ?: System.getenv("DB_URL")
            ?: return null
        return parseDatabaseUrl(rawUrl)
    }

    private fun parseDatabaseUrl(rawUrl: String): DbConfig {
        if (rawUrl.startsWith("jdbc:")) {
            return DbConfig(jdbcUrl = rawUrl, user = null, password = null)
        }
        val uri = URI(rawUrl)
        val userInfo = uri.userInfo?.split(":", limit = 2).orEmpty()
        val user = userInfo.getOrNull(0)
        val password = userInfo.getOrNull(1)
        val port = if (uri.port == -1) 5432 else uri.port
        val jdbcUrl = "jdbc:postgresql://${uri.host}:$port${uri.path}"
        return DbConfig(jdbcUrl = jdbcUrl, user = user, password = password)
    }
}

private data class DbConfig(
    val jdbcUrl: String,
    val user: String?,
    val password: String?
)
