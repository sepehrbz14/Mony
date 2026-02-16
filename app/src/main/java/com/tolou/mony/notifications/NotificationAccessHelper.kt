package com.tolou.mony.notifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

object NotificationAccessHelper {
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty()

        val componentName = ComponentName(context, BankNotificationListenerService::class.java)
        return enabledListeners.split(":").any { listener ->
            ComponentName.unflattenFromString(listener) == componentName
        }
    }

    fun buildSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
