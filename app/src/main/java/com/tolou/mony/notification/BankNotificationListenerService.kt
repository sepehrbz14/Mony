package com.tolou.mony.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

private const val TAG = "BankNotificationService"

class BankNotificationListenerService : NotificationListenerService() {

    private val parser = BankSmsParser()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) {
            return
        }

        val extras = sbn.notification.extras
        val title = extras?.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras?.getCharSequence("android.text")?.toString().orEmpty()
        val bigText = extras?.getCharSequence("android.bigText")?.toString().orEmpty()

        val mergedMessage = listOf(title, text, bigText)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(separator = "\n")

        if (!looksLikeBankMessage(mergedMessage)) {
            return
        }

        val parsed = parser.parse(mergedMessage)

        Log.i(
            TAG,
            "Parsed bank sms: template=${parser.detectTemplate(parsed.normalizedMessage)} " +
                "type=${parsed.type} amount=${parsed.amount} balance=${parsed.balance} " +
                "account=${parsed.accountNumber} raw=${parsed.rawMessage}"
        )

        // Next integration step:
        // persist transaction locally and/or sync to backend.
    }

    private fun looksLikeBankMessage(message: String): Boolean {
        if (message.isBlank()) {
            return false
        }

        val bankKeywords = listOf(
            "ریال",
            "حساب",
            "مانده",
            "موجودی",
            "برداشت",
            "واریز",
            "انتقال",
            "مبلغ"
        )

        return bankKeywords.any { keyword -> message.contains(keyword) }
    }
}
