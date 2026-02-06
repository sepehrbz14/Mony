plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.tolou.mony.server.ApplicationKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation("org.flywaydb:flyway-database-postgresql:12.0.0")
    implementation(libs.hikari)
    implementation(libs.flyway.core)
    implementation(libs.postgres)
    implementation(libs.jwt.library)
    implementation(libs.bcrypt)
    implementation(libs.logback.classic)
}
