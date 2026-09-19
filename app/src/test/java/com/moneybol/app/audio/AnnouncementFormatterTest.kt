package com.moneybol.app.audio

import com.moneybol.app.core.model.AnnouncementFormat
import com.moneybol.app.core.model.PaymentDirection
import com.moneybol.app.core.model.PaymentEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for AnnouncementFormatter.
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

    @Test
    fun `format payment received amount`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT
        )
        assertEquals("Payment received. five hundred rupees.", result)
    }

    @Test
    fun `format amount received`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.AMOUNT_RECEIVED
        )
        assertEquals("five hundred rupees received.", result)
    }

    @Test
    fun `format payment received Rs`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.PAYMENT_RECEIVED_RS
        )
        assertEquals("Payment received, Rs. 500.", result)
    }

    @Test
    fun `test announcement format`() {
        val result = AnnouncementFormatter.formatTest(AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT)
        assertEquals("Payment received. one hundred rupees.", result)
    }

    @Test
    fun `formatter does not include sensitive data`() {
        val result = AnnouncementFormatter.format(
            createEvent(50000),
            AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT
        )
        assertFalse(result.contains("OTP"))
        assertFalse(result.contains("PIN"))
        assertFalse(result.contains("password"))
        assertFalse(result.contains("TEST_001")) // transaction ID not in speech
    }
}
