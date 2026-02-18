package com.tolou.mony.notifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
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

    fun ensureListenerRunning(context: Context) {
        if (!isNotificationListenerEnabled(context)) return

        val componentName = ComponentName(context, BankNotificationListenerService::class.java)
        val packageManager = context.packageManager

        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationListenerService.requestRebind(componentName)
        }
    }
}
