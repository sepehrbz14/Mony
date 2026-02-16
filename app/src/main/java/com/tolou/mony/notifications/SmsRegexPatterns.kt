package com.tolou.mony.notifications

object SmsRegexPatterns {
    val signedNumber = Regex("([+-]\\s*\\d[\\d,٬٫]*)")
    val allNumber = Regex("(\\d[\\d,٬٫]*)")
    val accountAfterHesab = Regex("حساب\\s*[:：]?\\s*([\\d*]+)")
    val amountAfterMablagh = Regex("مبلغ\\s*[:：]?\\s*([+-]?\\s*\\d[\\d,٬٫]*)")
    val transferAccount = Regex("(?:انتقال\\s*به|برداشت\\s*از|واریز\\s*به)\\s*([\\d*]+)")
    val balanceAfterKeywords = Regex("(?:مانده|موجودی)\\s*[:：]?\\s*([+-]?\\s*\\d[\\d,٬٫]*)")
    val amountBeforeRial = Regex("([+-]?\\s*\\d[\\d,٬٫]*)\\s*ریال")

    val dateTime = Regex("(\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}\\s+\\d{1,2}:\\d{1,2}(?::\\d{1,2})?)")
    val dateOnly = Regex("(\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})")
    val timeOnly = Regex("(\\d{1,2}:\\d{1,2}(?::\\d{1,2})?)")
}
