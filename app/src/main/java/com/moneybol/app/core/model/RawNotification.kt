package com.moneybol.app.core.model

/**
 * Raw notification data extracted from Android's StatusBarNotification.
 * This is the first-stage model before any payment parsing.
 *
 * Sensitive content should NOT be permanently stored.
 */
data class RawNotification(
    val packageName: String,
    val appName: String? = null,
    val title: String? = null,
    val text: String? = null,
    val bigText: String? = null,
    val subText: String? = null,
    val timestamp: Long,
    val notificationKey: String? = null,
    val category: String? = null
)
