package com.tolou.mony.notifications

import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val balance: Long?,
    val accountNumber: String?,
    val dateTime: LocalDateTime?,
    val rawMessage: String,
    val normalizedMessage: String,
    val templateType: TemplateType
)

object BankSmsParser {
    private const val TAG = "BankSmsParser"

    fun parse(rawMessage: String): Transaction {
        val normalized = preprocess(rawMessage)
        val templateType = detectTemplate(normalized)

        return runCatching {
            when (templateType) {
                TemplateType.TYPE_1 -> parseTemplate1(normalized, rawMessage)
                TemplateType.TYPE_2 -> parseTemplate2(normalized, rawMessage)
                TemplateType.TYPE_3 -> parseTemplate3(normalized, rawMessage)
                TemplateType.FALLBACK -> parseFallback(normalized, rawMessage)
            }
        }.getOrElse {
            Log.w(TAG, "Failed to parse sms. raw=$rawMessage", it)
            Transaction(
                amount = 0L,
                type = ParsedTransactionType.UNKNOWN,
                balance = null,
                accountNumber = null,
                dateTime = null,
                rawMessage = rawMessage,
                normalizedMessage = normalized,
                templateType = TemplateType.FALLBACK
            )
        }
    }

    private fun preprocess(text: String): String {
        return normalizeWhitespaces(
            normalizeCurrencyWords(
                convertPersianDigitsToEnglish(text)
            )
        )
    }

    fun detectTemplate(text: String): TemplateType {
        val normalized = preprocess(text)
        val hasSignedNumber = SmsRegexPatterns.signedNumber.containsMatchIn(normalized)

        val hasBalanceKeyword = normalized.contains("مانده") || normalized.contains("موجودی")
        val hasAccountKeyword = normalized.contains("حساب")
        val hasBalanceValue = SmsRegexPatterns.balanceAfterKeywords.containsMatchIn(normalized)

        val isType1 = hasBalanceKeyword && hasSignedNumber && (hasAccountKeyword || hasBalanceValue)
        if (isType1) return TemplateType.TYPE_1

        val isType2 = normalized.contains("مبلغ") &&
            (normalized.contains("انتقال") || normalized.contains("برداشت"))
        if (isType2) return TemplateType.TYPE_2

        val isType3 = normalized.contains("ریال") &&
            normalized.contains("موجودی") &&
            (normalized.contains("برداشت پول") || normalized.contains("واریز پول"))
        if (isType3) return TemplateType.TYPE_3

        return TemplateType.FALLBACK
    }

    fun extractSignedNumbers(text: String): List<Long> {
        return SmsRegexPatterns.signedNumber
            .findAll(preprocess(text))
            .mapNotNull { parseLong(it.groupValues[1]) }
            .toList()
    }

    fun extractAllNumbers(text: String): List<Long> {
        return SmsRegexPatterns.allNumber
            .findAll(preprocess(text))
            .mapNotNull { parseLong(it.groupValues[1]) }
            .toList()
    }

    fun extractDate(text: String): LocalDateTime? {
        val normalized = preprocess(text)
        return extractLastDateTimeLine(normalized)
            ?: parseDateTimeOrDate(normalized)
    }

    fun extractAccountNumber(text: String): String? {
        val normalized = preprocess(text)
        return SmsRegexPatterns.accountAfterHesab.find(normalized)?.groupValues?.get(1)
            ?: SmsRegexPatterns.transferAccount.find(normalized)?.groupValues?.get(1)
    }

    fun parseTemplate1(text: String): Transaction = parseTemplate1(text, text)

    fun parseTemplate2(text: String): Transaction = parseTemplate2(text, text)

    fun parseTemplate3(text: String): Transaction = parseTemplate3(text, text)

    fun parseFallback(text: String): Transaction = parseFallback(text, text)

    private fun parseTemplate1(text: String, rawMessage: String): Transaction {
        val amount = extractSignedNumbers(text).firstOrNull() ?: 0L
        val balance = SmsRegexPatterns.balanceAfterKeywords
            .find(preprocess(text))
            ?.groupValues
            ?.get(1)
            ?.let(::parseLong)

        return Transaction(
            amount = amount,
            type = amount.toTransactionType(),
            balance = balance,
            accountNumber = extractAccountNumber(text),
            dateTime = extractLastDateTimeLine(preprocess(text)) ?: extractDate(text),
            rawMessage = rawMessage,
            normalizedMessage = preprocess(text),
            templateType = TemplateType.TYPE_1
        )
    }

    private fun parseTemplate2(text: String, rawMessage: String): Transaction {
        val normalized = preprocess(text)
        val amount = SmsRegexPatterns.amountAfterMablagh
            .find(normalized)
            ?.groupValues
            ?.get(1)
            ?.let(::parseLong)
            ?: extractAllNumbers(normalized).firstOrNull()
            ?: 0L

        val type = when {
            normalized.contains("انتقال به") -> ParsedTransactionType.EXPENSE
            normalized.contains("برداشت از") -> ParsedTransactionType.EXPENSE
            normalized.contains("واریز") -> ParsedTransactionType.INCOME
            else -> ParsedTransactionType.UNKNOWN
        }

        return Transaction(
            amount = amount,
            type = type,
            balance = null,
            accountNumber = extractAccountNumber(normalized),
            dateTime = extractDate(normalized),
            rawMessage = rawMessage,
            normalizedMessage = normalized,
            templateType = TemplateType.TYPE_2
        )
    }

    private fun parseTemplate3(text: String, rawMessage: String): Transaction {
        val normalized = preprocess(text)

        val amount = SmsRegexPatterns.amountBeforeRial
            .find(normalized)
            ?.groupValues
            ?.get(1)
            ?.let(::parseLong)
            ?: 0L

        val balance = SmsRegexPatterns.balanceAfterKeywords
            .find(normalized)
            ?.groupValues
            ?.get(1)
            ?.let(::parseLong)

        val type = when {
            normalized.contains("برداشت") -> ParsedTransactionType.EXPENSE
            normalized.contains("واریز") -> ParsedTransactionType.INCOME
            else -> ParsedTransactionType.UNKNOWN
        }

        return Transaction(
            amount = amount,
            type = type,
            balance = balance,
            accountNumber = extractAccountNumber(normalized),
            dateTime = extractDate(normalized),
            rawMessage = rawMessage,
            normalizedMessage = normalized,
            templateType = TemplateType.TYPE_3
        )
    }

    private fun parseFallback(text: String, rawMessage: String): Transaction {
        val signedNumbers = extractSignedNumbers(text)
        val amount = when {
            signedNumbers.isEmpty() -> 0L
            signedNumbers.size == 1 -> signedNumbers.first()
            else -> signedNumbers.first()
        }

        return Transaction(
            amount = amount,
            type = amount.toTransactionType(),
            balance = null,
            accountNumber = extractAccountNumber(text),
            dateTime = extractDate(text),
            rawMessage = rawMessage,
            normalizedMessage = preprocess(text),
            templateType = TemplateType.FALLBACK
        )
    }

    private fun Long.toTransactionType(): ParsedTransactionType {
        return when {
            this > 0 -> ParsedTransactionType.INCOME
            this < 0 -> ParsedTransactionType.EXPENSE
            else -> ParsedTransactionType.UNKNOWN
        }
    }

    private fun parseLong(rawNumber: String): Long? {
        val normalized = convertPersianDigitsToEnglish(rawNumber)
            .replace("٬", "")
            .replace(",", "")
            .replace("٫", "")
            .replace(" ", "")

        return normalized.toLongOrNull()
    }

    private fun normalizeCurrencyWords(text: String): String {
        return text.replace("ريال", "ریال")
    }

    private fun normalizeWhitespaces(text: String): String {
        return text
            .replace("\r\n", "\n")
            .replace(Regex("[\\t ]+"), " ")
            .replace(Regex("\\n+"), "\n")
            .trim()
    }

    private fun convertPersianDigitsToEnglish(text: String): String {
        val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
        val arabicDigits = "٠١٢٣٤٥٦٧٨٩"
        val englishDigits = "0123456789"

        val converted = buildString(text.length) {
            text.forEach { char ->
                val persianIndex = persianDigits.indexOf(char)
                val arabicIndex = arabicDigits.indexOf(char)
                when {
                    persianIndex >= 0 -> append(englishDigits[persianIndex])
                    arabicIndex >= 0 -> append(englishDigits[arabicIndex])
                    else -> append(char)
                }
            }
        }

        return converted
    }

    private fun extractLastDateTimeLine(text: String): LocalDateTime? {
        val lines = text.lines().asReversed()
        for (line in lines) {
            val parsed = parseDateTimeOrDate(line)
            if (parsed != null) return parsed
        }
        return null
    }

    private fun parseDateTimeOrDate(text: String): LocalDateTime? {
        val normalized = preprocess(text)

        SmsRegexPatterns.dateTime.find(normalized)?.groupValues?.get(1)?.let {
            parseDateTimeWithPatterns(it)?.let { parsed -> return parsed }
        }

        SmsRegexPatterns.monthDayUnderscoreTime.find(normalized)?.groupValues?.get(1)?.let {
            parseMonthDayTimeWithCurrentYear(it)?.let { parsed -> return parsed }
        }

        val date = SmsRegexPatterns.dateOnly.find(normalized)?.groupValues?.get(1)?.let(::parseDate)
        val time = SmsRegexPatterns.timeOnly.find(normalized)?.groupValues?.get(1)?.let(::parseTime)

        if (date != null && time != null) return LocalDateTime.of(date, time)
        if (date != null) return date.atStartOfDay()

        return null
    }

    private fun parseDateTimeWithPatterns(value: String): LocalDateTime? {
        val patterns = listOf(
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        )

        for (pattern in patterns) {
            val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            val parsed = runCatching { LocalDateTime.parse(value, formatter) }.getOrNull()
            if (parsed != null) return parsed
        }

        return null
    }


    private fun parseMonthDayTimeWithCurrentYear(value: String): LocalDateTime? {
        val parts = value.split("_")
        if (parts.size != 2) return null

        val dateParts = parts[0].split("/")
        if (dateParts.size != 2) return null

        val month = dateParts[0].toIntOrNull() ?: return null
        val day = dateParts[1].toIntOrNull() ?: return null
        val time = parseTime(parts[1]) ?: return null

        return runCatching {
            LocalDateTime.of(LocalDate.now().year, month, day, time.hour, time.minute, time.second)
        }.getOrNull()
    }

    private fun parseDate(value: String): LocalDate? {
        val patterns = listOf("yyyy/MM/dd", "yyyy-MM-dd")

        for (pattern in patterns) {
            val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            val parsed = runCatching { LocalDate.parse(value, formatter) }.getOrNull()
            if (parsed != null) return parsed
        }

        return null
    }

    private fun parseTime(value: String): LocalTime? {
        val patterns = listOf("H:mm:ss", "H:mm")

        for (pattern in patterns) {
            val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            val parsed = runCatching { LocalTime.parse(value, formatter) }.getOrNull()
            if (parsed != null) return parsed
        }

        return null
    }
}
