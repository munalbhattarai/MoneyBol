package com.moneybol.app.audio

import com.moneybol.app.core.model.AnnouncementFormat
import com.moneybol.app.core.model.PaymentDirection
import com.moneybol.app.core.model.PaymentEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Tests for AnnouncementFormatter in English and Nepali.
 */
class AnnouncementFormatterTest {

    private fun createEvent(amountPaisa: Long) = PaymentEvent(
        id = "test-1",
        provider = "esewa",
        providerDisplayName = "eSewa",
        amount = amountPaisa,
        currency = "NPR",
        transactionId = "TEST_001",
        payerName = null,
        timestamp = System.currentTimeMillis(),
        direction = PaymentDirection.CREDIT,
        confidence = 0.9f,
    )

    // ── English Formats ──

    @Test
    fun `format payment received amount in English`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT,
            "en"
        )
        assertEquals("Payment received. five hundred rupees.", result)
    }

    @Test
    fun `format amount received in English`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.AMOUNT_RECEIVED,
            "en"
        )
        assertEquals("five hundred rupees received.", result)
    }

    @Test
    fun `format payment received Rs in English`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.PAYMENT_RECEIVED_RS,
            "en"
        )
        assertEquals("Payment received, Rs. 500.", result)
    }

    @Test
    fun `test announcement format in English`() {
        val result = AnnouncementFormatter.formatTest(AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT, "en")
        assertEquals("Payment received. one hundred rupees.", result)
    }

    // ── Nepali Formats ──

    @Test
    fun `format payment received amount in Nepali - Rs 500`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT,
            "ne"
        )
        assertEquals("पाँच सय रुपैयाँ प्राप्त भयो।", result)
    }

    @Test
    fun `format payment received amount in Nepali - Rs 1500`() {
        val result = AnnouncementFormatter.format(
            createEvent(150000),
            AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT,
            "ne"
        )
        assertEquals("एक हजार पाँच सय रुपैयाँ प्राप्त भयो।", result)
    }

    @Test
    fun `format payment received amount in Nepali - Rs 10000`() {
        val result = AnnouncementFormatter.format(
            createEvent(1000000),
            AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT,
            "ne"
        )
        assertEquals("दस हजार रुपैयाँ प्राप्त भयो।", result)
    }

    @Test
    fun `format amount received in Nepali`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.AMOUNT_RECEIVED,
            "ne"
        )
        assertEquals("पाँच सय रुपैयाँ प्राप्त भयो।", result)
    }

    @Test
    fun `format payment received Rs in Nepali`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.PAYMENT_RECEIVED_RS,
            "ne"
        )
        assertEquals("रु. ५०० प्राप्त भयो।", result)
    }

    @Test
    fun `test announcement format in Nepali`() {
        val result = AnnouncementFormatter.formatTest(AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT, "ne")
        assertEquals("एक सय रुपैयाँ प्राप्त भयो।", result)
    }

    // ── Safety ──

    @Test
    fun `formatter does not include sensitive data`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT,
            "en"
        )
        assertFalse(result.contains("OTP"))
        assertFalse(result.contains("PIN"))
        assertFalse(result.contains("password"))
        assertFalse(result.contains("TEST_001"))
    }
}
