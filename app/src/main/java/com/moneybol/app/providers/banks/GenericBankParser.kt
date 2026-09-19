package com.moneybol.app.providers.banks

import com.moneybol.app.core.model.PaymentDirection
import com.moneybol.app.core.model.PaymentEvent
import com.moneybol.app.core.model.RawNotification
import com.moneybol.app.core.util.AmountParser
import com.moneybol.app.core.util.DirectionDetector
import com.moneybol.app.core.util.TransactionIdExtractor
import com.moneybol.app.providers.PaymentNotificationParser
import java.util.UUID

/**
 * Generic bank notification parser.
 *
 * This is the fallback parser for bank notifications where no verified
 * provider-specific parser exists.
 *
 * Uses conservative deterministic pattern matching:
 * - Must contain a recognizable amount (Rs/NPR/रु)
 * - Must have a clear credit direction indicator
 * - Higher confidence threshold required
 *
 * IMPORTANT: This parser uses conceptual keyword matching, NOT exact
 * bank SMS templates. Exact templates are unknown without verified samples.
 *
 * STATUS: FORMAT_UNVERIFIED
 */
class GenericBankParser : PaymentNotificationParser {

    override val providerId = "generic_bank"
    override val providerDisplayName = "Bank"

    /**
     * The generic parser does not match by specific package names.
     * It acts as a fallback for any notification that contains
     * payment-like patterns.
     */
    override val supportedPackageNames: Set<String> = emptySet()

    /**
     * Known banking/SMS app package names that may carry bank notifications.
     */
    private val bankRelatedPackages = setOf(
        // SMS apps
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms",
        // Known Nepali bank app package name patterns
        "com.nabilbank",
        "com.nmbmobilebanking",
        "com.gibl",
        "com.hbl",
        "com.siddharthabank",
        "com.nepalbank",
        "com.nmb",
        "com.prabhubank",
        "com.mbl",
    )

    override fun canHandle(notification: RawNotification): Boolean {
        val text = getCombinedText(notification)
        if (text.isBlank()) return false

        // Must have an amount
        val amount = AmountParser.parse(text) ?: return false

        // Must have a clear credit indicator
        val direction = DirectionDetector.detect(
            title = notification.title,
            text = notification.text,
            bigText = notification.bigText,
        )

        // Only handle clear credit cases
        return direction == PaymentDirection.CREDIT
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

        // Only parse credits
        if (direction != PaymentDirection.CREDIT) return null

        val transactionId = TransactionIdExtractor.extractFromNotification(
            title = notification.title,
            text = notification.text,
            bigText = notification.bigText,
        )

        // Determine provider name from app name or package
        val providerName = notification.appName ?: notification.packageName

        val confidence = calculateConfidence(notification, amount, transactionId)

        // Generic bank parser requires higher confidence
        if (confidence < 0.7f) return null

        return PaymentEvent(
            id = UUID.randomUUID().toString(),
            provider = "bank_${notification.packageName}",
            providerDisplayName = providerName,
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
        notification: RawNotification,
        amount: Long,
        transactionId: String?
    ): Float {
        var confidence = 0.4f

        // From a known banking package?
        val isBankPackage = bankRelatedPackages.any {
            notification.packageName.startsWith(it)
        }
        if (isBankPackage) confidence += 0.2f

        // Has transaction ID?
        if (transactionId != null) confidence += 0.15f

        // Reasonable amount?
        if (amount in 100..10_000_000_00L) confidence += 0.1f

        // Contains "account" keyword? (common in bank notifications)
        val text = getCombinedText(notification).lowercase()
        if (text.contains("account") || text.contains("a/c")) {
            confidence += 0.1f
        }

        // Contains "balance" keyword? (common in bank notifications)
        if (text.contains("balance") || text.contains("bal")) {
            confidence += 0.05f
        }

        return confidence.coerceIn(0f, 1f)
    }
}
