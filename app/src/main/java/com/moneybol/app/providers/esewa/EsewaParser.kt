package com.moneybol.app.providers.esewa

import com.moneybol.app.core.model.PaymentDirection
import com.moneybol.app.core.model.PaymentEvent
import com.moneybol.app.core.model.RawNotification
import com.moneybol.app.core.util.AmountParser
import com.moneybol.app.core.util.DirectionDetector
import com.moneybol.app.core.util.TransactionIdExtractor
import com.moneybol.app.providers.PaymentNotificationParser
import java.util.UUID

/**
 * eSewa payment notification parser.
 *
 * STATUS: FORMAT_UNVERIFIED
 *
 * This parser uses conservative pattern matching.
 * Exact notification formats have NOT been verified with real eSewa notifications.
 * Do not mark as VERIFIED until tested with sanitized real samples.
 *
 * Known: eSewa package is com.f1soft.esewa
 * Known: eSewa sends merchant notifications for successful transactions
 * Known: eSewa has transaction reference IDs and status values
 */
class EsewaParser : PaymentNotificationParser {

    override val providerId = "esewa"
    override val providerDisplayName = "eSewa"
    override val supportedPackageNames = setOf("com.f1soft.esewa")

    override fun canHandle(notification: RawNotification): Boolean {
        if (notification.packageName !in supportedPackageNames) return false
        val text = getCombinedText(notification)
        if (text.isBlank()) return false

        // Must contain an amount indicator
        return AmountParser.parse(text) != null
    }

    override fun parse(notification: RawNotification): PaymentEvent? {
        val text = getCombinedText(notification)
        if (text.isBlank()) return null

        // Parse amount
        val amount = AmountParser.parse(text) ?: return null

        // Detect direction
        val direction = DirectionDetector.detect(
            title = notification.title,
            text = notification.text,
            bigText = notification.bigText,
        )

        // Extract transaction ID
        val transactionId = TransactionIdExtractor.extractFromNotification(
            title = notification.title,
            text = notification.text,
            bigText = notification.bigText,
        )

        // Determine confidence based on what we could parse
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

        // Direction confidence boost
        if (direction == PaymentDirection.CREDIT) confidence += 0.2f

        // Having a transaction ID increases confidence
        if (transactionId != null) confidence += 0.1f

        // Reasonable amount range
        if (amount in 100..10_000_000_00L) confidence += 0.1f

        return confidence.coerceIn(0f, 1f)
    }
}
