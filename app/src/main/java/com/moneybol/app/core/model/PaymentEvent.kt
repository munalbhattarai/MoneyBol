package com.moneybol.app.core.model

/**
 * Represents a detected payment event after notification parsing.
 *
 * Amount is stored in paisa (1 rupee = 100 paisa) for precision.
 * For example, Rs. 500 = 50000 paisa.
 *
 * Confidence is 0.0–1.0 representing how confident the parser is
 * that this is a genuine incoming payment notification.
 */
data class PaymentEvent(
    val id: String,
    val provider: String,
    val providerDisplayName: String,
    val amount: Long,
    val currency: String = "NPR",
    val transactionId: String? = null,
    val payerName: String? = null,
    val timestamp: Long,
    val direction: PaymentDirection,
    val confidence: Float,
    val rawNotificationHash: String? = null
) {
    /**
     * Amount in rupees (for display purposes).
     */
    val amountInRupees: Double
        get() = amount / 100.0

    /**
     * Formatted amount string: "Rs. 500" or "Rs. 1,500.50"
     */
    val formattedAmount: String
        get() {
            val rupees = amount / 100
            val paisa = amount % 100
            return if (paisa == 0L) {
                "Rs. %,d".format(rupees)
            } else {
                "Rs. %,.2f".format(amount / 100.0)
            }
        }

    /**
     * Whether this event should trigger a voice announcement.
     * Only high-confidence incoming credits are announced.
     */
    val shouldAnnounce: Boolean
        get() = direction == PaymentDirection.CREDIT && confidence >= 0.7f
}
