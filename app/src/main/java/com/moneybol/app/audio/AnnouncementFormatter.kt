package com.moneybol.app.audio

import com.moneybol.app.core.model.AnnouncementFormat
import com.moneybol.app.core.model.PaymentEvent
import com.moneybol.app.core.util.AmountParser

/**
 * Formats payment events into speech text.
 *
 * SAFETY: Never includes OTPs, passwords, PINs, or arbitrary notification text.
 * Only announces amount and provider in configured format.
 */
object AnnouncementFormatter {

    /**
     * Format a payment event for TTS announcement.
     *
     * @param event The payment event to announce
     * @param format The preferred announcement format
     * @return Speech text to be spoken by TTS
     */
    fun format(event: PaymentEvent, format: AnnouncementFormat): String {
        val amountWords = AmountToWordsConverter.convert(event.amount)
        val amountNumeric = AmountParser.formatForDisplay(event.amount)

        return when (format) {
            AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT -> {
                "Payment received. $amountWords."
            }
            AnnouncementFormat.AMOUNT_RECEIVED -> {
                "$amountWords received."
            }
            AnnouncementFormat.PAYMENT_RECEIVED_RS -> {
                "Payment received, Rs. $amountNumeric."
            }
        }
    }

    /**
     * Format a test announcement.
     */
    fun formatTest(format: AnnouncementFormat): String {
        // Test with Rs. 100 (10000 paisa)
        return when (format) {
            AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT -> {
                "Payment received. one hundred rupees."
            }
            AnnouncementFormat.AMOUNT_RECEIVED -> {
                "one hundred rupees received."
            }
            AnnouncementFormat.PAYMENT_RECEIVED_RS -> {
                "Payment received, Rs. 100."
            }
        }
    }
}
