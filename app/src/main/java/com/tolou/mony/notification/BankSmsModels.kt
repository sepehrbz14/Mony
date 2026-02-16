package com.tolou.mony.notification

import java.time.LocalDateTime

enum class TemplateType {
    TYPE_1,
    TYPE_2,
    TYPE_3,
    FALLBACK
}

enum class ParsedTransactionType {
    INCOME,
    EXPENSE,
    UNKNOWN
}

data class Transaction(
    val amount: Long,
    val type: ParsedTransactionType,
    val balance: Long? = null,
    val accountNumber: String? = null,
    val dateTime: LocalDateTime? = null,
    val rawMessage: String,
    val normalizedMessage: String
)
