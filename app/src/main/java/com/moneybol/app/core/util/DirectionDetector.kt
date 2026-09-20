package com.moneybol.app.core.util

import com.moneybol.app.core.model.PaymentDirection

/**
 * Deterministic direction detector for payment notifications.
 *
 * Uses keyword-based pattern matching to determine if a notification
 * represents an incoming credit, outgoing debit, OTP, failed transaction, etc.
 *
 * Safety rule: If direction cannot be confidently determined → UNKNOWN.
 * UNKNOWN transactions are NOT announced.
 *
 * This is NOT AI-based. It is deterministic and testable.
 */
object DirectionDetector {

    /**
     * Keywords/phrases indicating incoming credit (money received).
     * Case-insensitive matching.
     */
    private val CREDIT_INDICATORS = listOf(
        "credited",
        "credit",
        "received",
        "deposit",
        "deposited",
        "incoming",
        "account credited",
        "amount received",
        "payment received",
        "money received",
        "fund transfer received",
        "has been credited",
        "been credited to",
        "credited to your",
        "credited to a/c",
        "credited by",
        "credited with",
        "credited for",
        "cash deposit",
        "remittance credit",
        "remittance received",
        "received from",
        // QR & Merchant transaction terms
        "fp qr transaction",
        "qr transaction",
        "qr payment",
        "qr received",
        "fonepay qr",
        "nepalpay qr",
        "merchant payment",
        "fund transfer from",
        "inward fund transfer",
        "inward remittance",
        "inward clearing",
        "ips credit",
        "connectips credit",
        "nchl credit",
        // Nepali terms
        "जम्मा",      // jamma (credited/deposited)
        "प्राप्त",     // praapt (received)
        "जम्मा भएको",
        "जम्मा भयो",
        "प्राप्त भयो",
    )

    /**
     * Keywords/phrases indicating outgoing debit (money sent).
     */
    private val DEBIT_INDICATORS = listOf(
        "debited",
        "debit",
        "sent",
        "withdrawn",
        "withdrawal",
        "cash withdrawal",
        "deducted",
        "deduction",
        "transfer out",
        "transferred from",
        "has been debited",
        "been debited from",
        "debited from your",
        "debited from a/c",
        "paid to",
        "payment to",
        "purchase",
        "purchased",
        "charge",
        "charged",
        "fee",
        // Nepali terms
        "कटौती",       // katauti (deducted)
        "भुक्तानी",    // bhuktani (payment made)
    )

    /**
     * Keywords indicating a failed, declined, or reversed transaction.
     * SAFETY: Reversals, cancellations, and failures must NEVER be announced.
     */
    private val FAILED_INDICATORS = listOf(
        "failed",
        "declined",
        "rejected",
        "unsuccessful",
        "not completed",
        "could not be processed",
        "transaction failed",
        "payment failed",
        "insufficient",
        "insufficient balance",
        "insufficient fund",
        "reversed",
        "reversal",
        "refunded",
        "refund",
        "cancelled",
        "canceled",
    )

    /**
     * Keywords indicating a pending transaction.
     */
    private val PENDING_INDICATORS = listOf(
        "pending",
        "processing",
        "in progress",
        "being processed",
        "under process",
        "awaiting",
        "on hold",
        "queued",
    )

    /**
     * Keywords indicating an OTP or verification code.
     * SAFETY: These must NEVER be announced.
     */
    private val OTP_INDICATORS = listOf(
        "otp",
        "one time password",
        "one-time password",
        "verification code",
        "verify",
        "verification",
        "security code",
        "login code",
        "mpin",
        "2fa",
        "two factor",
        "two-factor",
        "authenticate",
        "confirmation code",
    )

    /**
     * Detect the payment direction from notification text.
     *
     * @param title Notification title
     * @param text Notification text (body)
     * @param bigText Expanded notification text
     * @return Detected PaymentDirection
     */
    fun detect(
        title: String? = null,
        text: String? = null,
        bigText: String? = null
    ): PaymentDirection {
        // Combine all available text for analysis
        val combinedText = buildString {
            title?.let { append(it); append(" ") }
            text?.let { append(it); append(" ") }
            bigText?.let { append(it) }
        }.lowercase()

        if (combinedText.isBlank()) return PaymentDirection.UNKNOWN

        // OTP check first — highest priority safety check
        if (matchesAny(combinedText, OTP_INDICATORS)) {
            return PaymentDirection.OTP
        }

        // Failed check
        if (matchesAny(combinedText, FAILED_INDICATORS)) {
            return PaymentDirection.FAILED
        }

        // Pending check
        if (matchesAny(combinedText, PENDING_INDICATORS)) {
            return PaymentDirection.PENDING
        }

        // Now check credit vs debit
        val creditScore = scoreMatches(combinedText, CREDIT_INDICATORS)
        val debitScore = scoreMatches(combinedText, DEBIT_INDICATORS)

        return when {
            creditScore > 0 && debitScore == 0 -> PaymentDirection.CREDIT
            debitScore > 0 && creditScore == 0 -> PaymentDirection.DEBIT
            // Both present or neither — ambiguous → UNKNOWN (safe default)
            else -> PaymentDirection.UNKNOWN
        }
    }

    /**
     * Check if the combined text matches any indicator.
     */
    private fun matchesAny(text: String, indicators: List<String>): Boolean {
        return indicators.any { indicator ->
            text.contains(indicator.lowercase())
        }
    }

    /**
     * Count how many indicators match in the text.
     * Used to resolve credit vs debit when both have some matches.
     */
    private fun scoreMatches(text: String, indicators: List<String>): Int {
        return indicators.count { indicator ->
            text.contains(indicator.lowercase())
        }
    }
}
