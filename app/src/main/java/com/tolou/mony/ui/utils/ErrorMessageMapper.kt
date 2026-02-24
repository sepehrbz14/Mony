package com.tolou.mony.ui.utils

import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

fun Throwable.toUserMessage(defaultMessage: String = "Something went wrong."): String {
    return when (this) {
        is UnknownHostException, is IOException -> "Connection Error"
        is HttpException -> {
            when (code()) {
                401 -> "Your username or password is incorrect."
                409 -> "This phone number is already registered."
                in 500..599 -> "Server Error"
                else -> defaultMessage
            }
        }
        else -> defaultMessage
    }
}
