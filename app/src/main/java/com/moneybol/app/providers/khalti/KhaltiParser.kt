package com.moneybol.app.providers.khalti

import com.moneybol.app.core.model.PaymentDirection
import com.moneybol.app.core.model.PaymentEvent
import com.moneybol.app.core.model.RawNotification
import com.moneybol.app.core.util.AmountParser
import com.moneybol.app.core.util.DirectionDetector
import com.moneybol.app.core.util.TransactionIdExtractor
import com.moneybol.app.providers.PaymentNotificationParser
import java.util.UUID

/**
 * Khalti payment notification parser.
 *
 * STATUS: FORMAT_UNVERIFIED
 *
 * Known: Khalti package is com.khalti
 * Known: Khalti sends push notifications for successful transactions
 * Known: Khalti Sound Box announces payments (separate hardware product)
 *
 * Do not mark as VERIFIED until tested with sanitized real samples.
 */
class KhaltiParser : PaymentNotificationParser {

    override val providerId = "khalti"
    override val providerDisplayName = "Khalti"
    override val supportedPackageNames = setOf("com.khalti")

    override fun canHandle(notification: RawNotification): Boolean {
        if (notification.packageName !in supportedPackageNames) return false
        val text = getCombinedText(notification)
        if (text.isBlank()) return false
        return AmountParser.parse(text) != null
    }

    override fun parse(notification: RawNotification): PaymentEvent? {
        val text = getCombinedText(notification)
        if (text.isBlank()) return null

        val amount = AmountParser.parse(text) ?: return null

        val direction = DirectionDetector.detect(
            title = notification.title,
            text = notification.text,
            bigText = notification.bigText,
        )

        val transactionId = TransactionIdExtractor.extractFromNotification(
            title = notification.title,
            text = notification.text,
            bigText = notification.bigText,
        )

        val confidence = calculateConfidence(direction, amount, transactionId)

        return PaymentEvent(
            id = UUID.randomUUID().toString(),
            provider = providerId,
            providerDisplayName = providerDisplayName,
            amount = amount,
            currency = "NPR",
            transactionId = transactionId,
            payerName = null,
            timestamp = notification.timestamp,
            direction = direction,
            confidence = confidence,
        )
    }

    private fun getCombinedText(notification: RawNotification): String {
        return buildString {
            notification.title?.let { append(it); append(" ") }
            notification.text?.let { append(it); append(" ") }
            notification.bigText?.let { append(it) }
        }
    }

    private fun calculateConfidence(
        direction: PaymentDirection,
        amount: Long,
        transactionId: String?
    ): Float {
        var confidence = 0.5f
        if (direction == PaymentDirection.CREDIT) confidence += 0.2f
        if (transactionId != null) confidence += 0.1f
        if (amount in 100..10_000_000_00L) confidence += 0.1f
        return confidence.coerceIn(0f, 1f)
    }
}
