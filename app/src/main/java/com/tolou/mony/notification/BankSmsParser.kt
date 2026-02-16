package com.tolou.mony.notification

import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private const val TAG = "BankSmsParser"

class BankSmsParser(
    customParsers: Map<TemplateType, (String, String) -> Transaction> = emptyMap()
) {

    private val templateParsers: Map<TemplateType, (String, String) -> Transaction> = mapOf(
        TemplateType.TYPE_1 to ::parseTemplate1,
        TemplateType.TYPE_2 to ::parseTemplate2,
        TemplateType.TYPE_3 to ::parseTemplate3,
        TemplateType.FALLBACK to ::parseFallback
    ) + customParsers

    fun parse(rawMessage: String): Transaction {
        val normalized = BankSmsPreprocessor.normalize(rawMessage)
        return try {
            val templateType = detectTemplate(normalized)
            val templateParser = templateParsers[templateType] ?: ::parseFallback
            templateParser(normalized, rawMessage)
        } catch (exception: Exception) {
            Log.w(TAG, "Parsing failed, returning UNKNOWN transaction. raw=$rawMessage", exception)
            Transaction(
                amount = 0,
                type = ParsedTransactionType.UNKNOWN,
                rawMessage = rawMessage,
                normalizedMessage = normalized
            )
        }
    }

    fun detectTemplate(text: String): TemplateType {
        val hasSignedNumber = BankSmsRegexPatterns.SIGNED_NUMBER.containsMatchIn(text)

        val isType1 = text.contains("حساب") &&
            (text.contains("مانده") || text.contains("موجودی")) &&
            hasSignedNumber

        if (isType1) {
            return TemplateType.TYPE_1
        }

        val isType2 = text.contains("مبلغ") &&
            (text.contains("انتقال") || text.contains("برداشت"))

        if (isType2) {
            return TemplateType.TYPE_2
        }

        val isType3 = text.contains("ریال") &&
            text.contains("موجودی") &&
            (text.contains("برداشت پول") || text.contains("واریز پول"))

        if (isType3) {
            return TemplateType.TYPE_3
        }

        return TemplateType.FALLBACK
    }

    fun parseTemplate1(text: String, rawMessage: String = text): Transaction {
        val signedNumbers = extractSignedNumbers(text)
        val amount = signedNumbers.firstOrNull() ?: 0L
        val balance = extractBalance(text)
        val accountNumber = extractAccountNumber(text)
        val date = extractDate(text)

        return Transaction(
            amount = amount,
            type = amountToType(amount),
            balance = balance,
            accountNumber = accountNumber,
            dateTime = date,
            rawMessage = rawMessage,
            normalizedMessage = text
        )
    }

    fun parseTemplate2(text: String, rawMessage: String = text): Transaction {
        val amount = BankSmsRegexPatterns.AMOUNT_AFTER_MABLAGH.find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.sanitizeNumber()
            ?: 0L

        val type = when {
            text.contains("انتقال به") -> ParsedTransactionType.EXPENSE
            text.contains("برداشت از") -> ParsedTransactionType.EXPENSE
            text.contains("واریز") -> ParsedTransactionType.INCOME
            else -> ParsedTransactionType.UNKNOWN
        }

        val accountNumber = BankSmsRegexPatterns.TYPE2_ACCOUNT_AFTER_TRANSFER.find(text)
            ?.groupValues
            ?.getOrNull(1)

        return Transaction(
            amount = amount,
            type = if (amount == 0L && type == ParsedTransactionType.UNKNOWN) ParsedTransactionType.UNKNOWN else type,
            balance = null,
            accountNumber = accountNumber,
            dateTime = extractDate(text),
            rawMessage = rawMessage,
            normalizedMessage = text
        )
    }

    fun parseTemplate3(text: String, rawMessage: String = text): Transaction {
        val amount = BankSmsRegexPatterns.TYPE3_AMOUNT_BEFORE_RIAL.find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.sanitizeNumber()
            ?: 0L

        val balance = extractBalance(text)

        val type = when {
            text.contains("برداشت") -> ParsedTransactionType.EXPENSE
            text.contains("واریز") -> ParsedTransactionType.INCOME
            else -> ParsedTransactionType.UNKNOWN
        }

        return Transaction(
            amount = amount,
            type = type,
            balance = balance,
            accountNumber = extractAccountNumber(text),
            dateTime = extractDate(text),
            rawMessage = rawMessage,
            normalizedMessage = text
        )
    }

    fun parseFallback(text: String, rawMessage: String = text): Transaction {
        val signedNumbers = extractSignedNumbers(text)

        val amount = when {
            signedNumbers.isEmpty() -> 0L
            signedNumbers.size == 1 -> signedNumbers.first()
            else -> signedNumbers.first()
        }

        val type = when {
            amount < 0 -> ParsedTransactionType.EXPENSE
            amount > 0 -> ParsedTransactionType.INCOME
            else -> ParsedTransactionType.UNKNOWN
        }

        return Transaction(
            amount = amount,
            type = type,
            balance = null,
            accountNumber = extractAccountNumber(text),
            dateTime = null,
            rawMessage = rawMessage,
            normalizedMessage = text
        )
    }

    fun extractSignedNumbers(text: String): List<Long> {
        return BankSmsRegexPatterns.SIGNED_NUMBER.findAll(text)
            .mapNotNull { match ->
                val sign = match.groupValues.getOrNull(1).orEmpty()
                val numberValue = match.groupValues.getOrNull(2)?.sanitizeNumber()
                numberValue?.let { value -> if (sign == "-") -value else value }
            }
            .toList()
    }

    fun extractAllNumbers(text: String): List<Long> {
        return BankSmsRegexPatterns.ANY_NUMBER.findAll(text)
            .mapNotNull { it.value.sanitizeNumber() }
            .toList()
    }

    fun extractDate(text: String): LocalDateTime? {
        val dateMatch = BankSmsRegexPatterns.DATE_LINE.findAll(text).lastOrNull() ?: return null
        val dateRaw = dateMatch.value.replace('.', '/').replace('-', '/')
        val dateParts = dateRaw.split("/")

        if (dateParts.size != 3) {
            return null
        }

        val year = dateParts[0].toIntOrNull() ?: return null
        val month = dateParts[1].toIntOrNull() ?: return null
        val day = dateParts[2].toIntOrNull() ?: return null

        val timeMatch = BankSmsRegexPatterns.TIME_LINE.findAll(text).lastOrNull()?.value
        val parsedTime = timeMatch?.let { parseTime(it) } ?: LocalTime.MIDNIGHT

        return runCatching {
            LocalDateTime.of(LocalDate.of(year, month, day), parsedTime)
        }.getOrNull()
    }

    fun extractAccountNumber(text: String): String? {
        return BankSmsRegexPatterns.ACCOUNT_AFTER_HESAB.find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?: BankSmsRegexPatterns.TYPE2_ACCOUNT_AFTER_TRANSFER.find(text)
                ?.groupValues
                ?.getOrNull(1)
    }

    private fun extractBalance(text: String): Long? {
        return BankSmsRegexPatterns.BALANCE_AFTER_MANDE.find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.sanitizeNumber()
    }

    private fun amountToType(amount: Long): ParsedTransactionType {
        return when {
            amount < 0 -> ParsedTransactionType.EXPENSE
            amount > 0 -> ParsedTransactionType.INCOME
            else -> ParsedTransactionType.UNKNOWN
        }
    }

    private fun String.sanitizeNumber(): Long? {
        return replace(",", "").trim().toLongOrNull()
    }

    private fun parseTime(value: String): LocalTime? {
        val pieces = value.split(":")
        if (pieces.size !in 2..3) {
            return null
        }

        val hour = pieces[0].toIntOrNull() ?: return null
        val minute = pieces[1].toIntOrNull() ?: return null
        val second = pieces.getOrNull(2)?.toIntOrNull() ?: 0

        return runCatching { LocalTime.of(hour, minute, second) }.getOrNull()
    }
}
