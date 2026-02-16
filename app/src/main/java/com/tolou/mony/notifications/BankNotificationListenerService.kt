package com.tolou.mony.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class BankNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getString("android.title").orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        val bigText = extras.getCharSequence("android.bigText")?.toString().orEmpty()

        val rawMessage = listOf(title, bigText, text)
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n")
            .trim()

        if (rawMessage.isBlank()) return

        val parsed = BankSmsParser.parse(rawMessage)

        Log.d(
            "BankNotificationListener",
            "template=${parsed.templateType}, type=${parsed.type}, amount=${parsed.amount}, raw=${parsed.rawMessage}"
        )

        val isSupportedTemplate = parsed.templateType != TemplateType.FALLBACK
        if (!isSupportedTemplate || parsed.amount == 0L || parsed.type == ParsedTransactionType.UNKNOWN) {
            return
        }

        TransactionDetectionNotifier.notifyDetectedTransaction(this, parsed)
    }
}
