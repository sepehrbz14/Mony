package com.tolou.mony.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BankSmsParserTest {

    private val parser = BankSmsParser()

    @Test
    fun `detect type 1 and parse it`() {
        val message = """
            بانک نمونه
            حساب 12345678
            -12,000
            مانده 40,000
            1403/10/20
        """.trimIndent()

        val parsed = parser.parse(message)

        assertEquals(TemplateType.TYPE_1, parser.detectTemplate(parsed.normalizedMessage))
        assertEquals(-12000L, parsed.amount)
        assertEquals(40000L, parsed.balance)
        assertEquals("12345678", parsed.accountNumber)
        assertEquals(ParsedTransactionType.EXPENSE, parsed.type)
        assertNotNull(parsed.dateTime)
    }

    @Test
    fun `detect type 2 and parse it`() {
        val message = """
            انتقال به 99887766
            مبلغ: 150000
        """.trimIndent()

        val parsed = parser.parse(message)

        assertEquals(TemplateType.TYPE_2, parser.detectTemplate(parsed.normalizedMessage))
        assertEquals(150000L, parsed.amount)
        assertEquals("99887766", parsed.accountNumber)
        assertEquals(ParsedTransactionType.EXPENSE, parsed.type)
    }

    @Test
    fun `detect type 3 and parse it`() {
        val message = """
            برداشت پول
            کاربر عزیز، 250000 ریال برداشت شد
            موجودی: 750000 ریال
            21:32
            1403/11/01
        """.trimIndent()

        val parsed = parser.parse(message)

        assertEquals(TemplateType.TYPE_3, parser.detectTemplate(parsed.normalizedMessage))
        assertEquals(250000L, parsed.amount)
        assertEquals(750000L, parsed.balance)
        assertEquals(ParsedTransactionType.EXPENSE, parsed.type)
        assertNotNull(parsed.dateTime)
    }

    @Test
    fun `fallback parser handles minimal signed sms`() {
        val parsed = parser.parse("-480000")

        assertEquals(TemplateType.FALLBACK, parser.detectTemplate(parsed.normalizedMessage))
        assertEquals(-480000L, parsed.amount)
        assertEquals(ParsedTransactionType.EXPENSE, parsed.type)
        assertEquals(null, parsed.balance)
    }

    @Test
    fun `normalizer converts persian digits and currency`() {
        val normalized = BankSmsPreprocessor.normalize("مبلغ: ۱۲۳۴ ريال")

        assertEquals("مبلغ: 1234 ریال", normalized)
    }
}
