package com.tolou.mony.notification

object BankSmsRegexPatterns {
    val SIGNED_NUMBER = Regex("([+-])\\s*([0-9][0-9,]*)")
    val ANY_NUMBER = Regex("[0-9][0-9,]*")
    val ACCOUNT_AFTER_HESAB = Regex("حساب\\s*:?\\s*([0-9*]+)")
    val AMOUNT_AFTER_MABLAGH = Regex("مبلغ\\s*:?\\s*([0-9][0-9,]*)")
    val BALANCE_AFTER_MANDE = Regex("(?:مانده|موجودی)\\s*:?\\s*([0-9][0-9,]*)")
    val TYPE2_ACCOUNT_AFTER_TRANSFER = Regex("(?:انتقال\\s+به|برداشت\\s+از)\\s*:?\\s*([0-9*]+)")
    val DATE_LINE = Regex("(13|14)\\d{2}[/.-](0?[1-9]|1[0-2])[/.-](0?[1-9]|[12]\\d|3[01])")
    val TIME_LINE = Regex("([01]?\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d)?")
    val TYPE3_AMOUNT_BEFORE_RIAL = Regex("([0-9][0-9,]*)\\s*ریال")
}
