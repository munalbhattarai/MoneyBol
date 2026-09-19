package com.moneybol.app.audio

import com.moneybol.app.core.model.AnnouncementFormat
import com.moneybol.app.core.model.PaymentEvent
import com.moneybol.app.core.util.AmountParser
import com.moneybol.app.core.util.NepaliNumberConverter

/**
 * Formats payment events into speech text in English or Nepali.
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
     * @param language The language code ("en" for English, "ne" for Nepali)
     * @return Speech text to be spoken by TTS
     */
    fun format(
        event: PaymentEvent,
        format: AnnouncementFormat,
        language: String = "en"
    ): String {
        return if (language == "ne") {
            val amountWords = NepaliAmountToWordsConverter.convert(event.amount)
            val amountNumeric = NepaliNumberConverter.toNepali(
                AmountParser.formatForDisplay(event.amount)
            )

            when (format) {
                AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT -> {
                    "$amountWords प्राप्त भयो।"
                }
                AnnouncementFormat.AMOUNT_RECEIVED -> {
                    "$amountWords प्राप्त भयो।"
                }
                AnnouncementFormat.PAYMENT_RECEIVED_RS -> {
                    "रु. $amountNumeric प्राप्त भयो।"
                }
            }
        } else {
            val amountWords = AmountToWordsConverter.convert(event.amount)
            val amountNumeric = AmountParser.formatForDisplay(event.amount)

            when (format) {
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
    }

    /**
     * Format a test announcement.
     *
     * @param format The preferred announcement format
     * @param language The language code ("en" for English, "ne" for Nepali)
     */
    fun formatTest(
        format: AnnouncementFormat,
        language: String = "en"
    ): String {
        return if (language == "ne") {
            when (format) {
                AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT -> {
                    "एक सय रुपैयाँ प्राप्त भयो।"
                }
                AnnouncementFormat.AMOUNT_RECEIVED -> {
                    "एक सय रुपैयाँ प्राप्त भयो।"
                }
                AnnouncementFormat.PAYMENT_RECEIVED_RS -> {
                    "रु. १०० प्राप्त भयो।"
                }
            }
        } else {
            when (format) {
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
}
