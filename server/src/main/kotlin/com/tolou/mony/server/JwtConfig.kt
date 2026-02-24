package com.tolou.mony.server

import io.ktor.server.config.ApplicationConfig

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val secret: String,
    val realm: String
) {
    companion object {
        fun fromConfig(config: ApplicationConfig): JwtConfig {
            return JwtConfig(
                issuer = config.property("jwt.issuer").getString(),
                audience = config.property("jwt.audience").getString(),
                secret = config.property("jwt.secret").getString(),
                realm = config.property("jwt.realm").getString()
            )
        }
    }
}
