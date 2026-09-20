package com.moneybol.app.notification

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.moneybol.app.core.model.RawNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MoneyBol Notification Listener Service.
 *
 * This service extracts raw notification data from incoming system notifications,
 * including rich text from MessagingStyle (Google Messages, SMS apps), InboxStyle,
 * BigTextStyle, and standard notifications.
 *
 * NEVER perform heavy work or payment logic here.
 * Delegates all processing to NotificationProcessor.
 */
@AndroidEntryPoint
class MoneyBolNotificationService : NotificationListenerService() {

    @Inject
    lateinit var notificationProcessor: NotificationProcessor

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence("android.title.big")?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()

        var text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        var bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        // 1. Extract messages from NotificationCompat.MessagingStyle (Google Messages, Xiaomi MMS, etc.)
        val messagesList = mutableListOf<String>()
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        if (messages != null) {
            for (item in messages) {
                if (item is Bundle) {
                    val msgText = item.getCharSequence("text")?.toString()
                    if (!msgText.isNullOrBlank()) {
                        messagesList.add(msgText)
                    }
                }
            }
        }

        // 2. Extract lines from NotificationCompat.InboxStyle
        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (textLines != null) {
            for (line in textLines) {
                val lineStr = line?.toString()
                if (!lineStr.isNullOrBlank()) {
                    messagesList.add(lineStr)
                }
            }
        }

        // 3. Extract tickerText if present
        val ticker = notification.tickerText?.toString()
        if (!ticker.isNullOrBlank()) {
            messagesList.add(ticker)
        }

        // Combine extracted messages into bigText and text if needed
        if (messagesList.isNotEmpty()) {
            val combinedMessages = messagesList.joinToString(" ")
            bigText = if (bigText.isNullOrBlank()) {
                combinedMessages
            } else {
                "$bigText $combinedMessages"
            }
            // If text is null, empty, or just a count like "3 new messages", use the actual message text
            if (text.isNullOrBlank() || text.matches(Regex("""(?i)\d+\s+new\s+messages?"""))) {
                text = messagesList.lastOrNull() ?: combinedMessages
            }
        }

        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()

        val rawNotification = RawNotification(
            packageName = sbn.packageName ?: return,
            appName = extras.getString("android.appName")
                ?: getAppName(sbn.packageName),
            title = title,
            text = text,
            bigText = bigText,
            subText = subText,
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
