package com.moneybol.app.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.moneybol.app.core.model.RawNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MoneyBol Notification Listener Service.
 *
 * This service is thin — it only extracts raw notification data
 * and delegates all processing to NotificationProcessor.
 *
 * NEVER perform heavy work or payment logic here.
 */
@AndroidEntryPoint
class MoneyBolNotificationService : NotificationListenerService() {

    @Inject
    lateinit var notificationProcessor: NotificationProcessor

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val rawNotification = RawNotification(
            packageName = sbn.packageName ?: return,
            appName = extras.getString("android.appName")
                ?: getAppName(sbn.packageName),
            title = extras.getCharSequence("android.title")?.toString(),
            text = extras.getCharSequence("android.text")?.toString(),
            bigText = extras.getCharSequence("android.bigText")?.toString(),
            subText = extras.getCharSequence("android.subText")?.toString(),
            timestamp = sbn.postTime,
            notificationKey = sbn.key,
            category = notification.category,
        )

        // Delegate processing off the main thread
        notificationProcessor.process(rawNotification)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action needed when notifications are removed
    }

    private fun getAppName(packageName: String): String? {
        return try {
            val pm = applicationContext.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "MoneyBolNotifService"
    }
}
