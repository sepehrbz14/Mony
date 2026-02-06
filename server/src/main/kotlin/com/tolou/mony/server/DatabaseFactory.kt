package com.tolou.mony.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    operator fun invoke(config: ApplicationConfig) {
        val url = config.propertyOrNull("database.url")?.getString()
            ?: "jdbc:postgresql://localhost:5432/mony"
        val user = config.propertyOrNull("database.user")?.getString() ?: "mony"
        val password = config.propertyOrNull("database.password")?.getString() ?: "mony"

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
            .load()
            .migrate()
    }
}
