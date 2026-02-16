package com.tolou.mony.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val rawMessage = messages.joinToString(separator = "\n") { it.messageBody.orEmpty() }.trim()
        if (rawMessage.isBlank()) return

        val parsed = BankSmsParser.parse(rawMessage)
        val isSupportedTemplate = parsed.templateType != TemplateType.FALLBACK
        if (!isSupportedTemplate || parsed.amount == 0L || parsed.type == ParsedTransactionType.UNKNOWN) {
            return
        }

        Log.d("SmsBroadcastReceiver", "Detected SMS transaction amount=${parsed.amount}")
        TransactionDetectionNotifier.notifyDetectedTransaction(context, parsed)
    }
}
