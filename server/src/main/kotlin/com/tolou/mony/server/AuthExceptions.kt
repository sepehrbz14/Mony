package com.tolou.mony.server

import io.ktor.http.HttpStatusCode

open class AuthHttpException(
    message: String,
    val statusCode: HttpStatusCode
) : RuntimeException(message)

class AuthConflictException(message: String) : AuthHttpException(message, HttpStatusCode.Conflict)
