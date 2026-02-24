package com.tolou.mony.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BankSmsParserTest {

    @Test
    fun `detect template 1`() {
        val sms = """
            بانک نمونه
            حساب 123456
            -450000
            مانده 1200000
            2024/12/25 14:33
        """.trimIndent()

        val type = BankSmsParser.detectTemplate(sms)

        assertEquals(TemplateType.TYPE_1, type)
    }

    @Test
    fun `parse template 1 returns amount balance account and expense type`() {
        val sms = """
            بانک نمونه
            حساب ۱۲۳۴۵۶
            -۴۵۰٬۰۰۰
            موجودی ۱٬۲۰۰٬۰۰۰
            2024/12/25 14:33
        """.trimIndent()

        val transaction = BankSmsParser.parseTemplate1(sms)

        assertEquals(-450000L, transaction.amount)
        assertEquals(1200000L, transaction.balance)
        assertEquals("123456", transaction.accountNumber)
        assertEquals(ParsedTransactionType.EXPENSE, transaction.type)
        assertEquals(TemplateType.TYPE_1, transaction.templateType)
    }

    @Test
    fun `parse template 2 returns amount and expense type`() {
        val sms = """
            انتقال به 778899
            مبلغ: 650000
            2024/12/20 10:00
        """.trimIndent()

        val transaction = BankSmsParser.parseTemplate2(sms)

        assertEquals(650000L, transaction.amount)
        assertEquals(ParsedTransactionType.EXPENSE, transaction.type)
        assertEquals("778899", transaction.accountNumber)
        assertNull(transaction.balance)
        assertEquals(TemplateType.TYPE_2, transaction.templateType)
    }



    @Test
    fun `detects type 1 when mablagh has trailing plus sign`() {
        val sms = """
            80001914726000
            مبلغ:500,000+
            مانده:27,328,965
            12/06
            00:59
        """.trimIndent()

        val type = BankSmsParser.detectTemplate(sms)

        assertEquals(TemplateType.TYPE_1, type)
    }

    @Test
    fun `parse type 1 with mablagh trailing plus sign`() {
        val sms = """
            80001914726000
            مبلغ:500,000+
            مانده:27,328,965
            12/06
            00:59
        """.trimIndent()

        val transaction = BankSmsParser.parse(sms)

        assertEquals(TemplateType.TYPE_1, transaction.templateType)
        assertEquals(50000L, transaction.amount)
        assertEquals(2732896L, transaction.balance)
        assertEquals(ParsedTransactionType.INCOME, transaction.type)
    }

    @Test
    fun `detect template 3 for parid and neshast phrases`() {
        val sms = """
            بانک نمونه
            640000 ریال از حساب شما پرید
            موجودی 9100000 ریال
            2024/12/25 14:33
        """.trimIndent()

        val type = BankSmsParser.detectTemplate(sms)

        assertEquals(TemplateType.TYPE_3, type)
    }

    @Test
    fun `parse template 3 returns amount balance and income type`() {
        val sms = """
            واریز پول
            علی عزیز، 250000 ریال به حساب شما واریز شد.
            موجودی: 980000 ریال
            09:45
            2024/12/25
        """.trimIndent()

        val transaction = BankSmsParser.parseTemplate3(sms)

        assertEquals(250000L, transaction.amount)
        assertEquals(980000L, transaction.balance)
        assertEquals(ParsedTransactionType.INCOME, transaction.type)
        assertEquals(TemplateType.TYPE_3, transaction.templateType)
    }

    @Test
    fun `fallback parses signed amount`() {
        val sms = "-480000"

        val transaction = BankSmsParser.parseFallback(sms)

        assertEquals(-480000L, transaction.amount)
        assertEquals(ParsedTransactionType.EXPENSE, transaction.type)
        assertEquals(TemplateType.FALLBACK, transaction.templateType)
    }

    @Test
    fun `parse unknown does not crash and returns unknown for no numbers`() {
        val sms = "پیام بدون عدد"

        val transaction = BankSmsParser.parse(sms)

        assertEquals(0L, transaction.amount)
        assertEquals(ParsedTransactionType.UNKNOWN, transaction.type)
    }

    @Test
    fun `detect and parse balance format without hesab`() {
        val sms = """
            777.888.24033008.1
            +41,401,031
            11/26_21:55
            مانده: 42,211,570
        """.trimIndent()

        val template = BankSmsParser.detectTemplate(sms)
        val transaction = BankSmsParser.parse(sms)

        assertEquals(TemplateType.TYPE_1, template)
        assertEquals(TemplateType.TYPE_1, transaction.templateType)
        assertEquals(41401031L, transaction.amount)
        assertEquals(42211570L, transaction.balance)
        assertEquals(ParsedTransactionType.INCOME, transaction.type)
    }

}
