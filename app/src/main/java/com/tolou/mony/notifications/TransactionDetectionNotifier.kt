package com.tolou.mony.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tolou.mony.R
import com.tolou.mony.ui.utils.formatRial
import kotlin.random.Random

object TransactionDetectionNotifier {
    private const val CHANNEL_ID = "transaction_detection_channel"

    fun notifyDetectedTransaction(context: Context, transaction: Transaction, pendingId: String) {
        createChannel(context)

        val saveIntent = Intent(context, SmsTransactionPromptActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(SmsTransactionPromptActivity.EXTRA_AMOUNT, transaction.amount)
            putExtra(SmsTransactionPromptActivity.EXTRA_TRANSACTION_TYPE, transaction.type.name)
            putExtra(SmsTransactionPromptActivity.EXTRA_PENDING_ID, pendingId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            Random.nextInt(),
            saveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = "Detected ${formatRial(transaction.amount)}. Tap to review and save"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Transaction detected")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Transaction Detection",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for detected bank SMS transactions"
        }
        manager.createNotificationChannel(channel)
    }
}
