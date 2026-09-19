package com.moneybol.app.payments

import com.moneybol.app.core.model.PaymentDirection
import com.moneybol.app.core.model.PaymentEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for DuplicateDetector.
 */
class DuplicateDetectorTest {

    private lateinit var detector: DuplicateDetector

    @Before
    fun setup() {
        detector = DuplicateDetector()
    }

    private fun createEvent(
        provider: String = "esewa",
        amount: Long = 50000,
        transactionId: String? = null,
        timestamp: Long = System.currentTimeMillis(),
    ) = PaymentEvent(
        id = "test-${System.nanoTime()}",
        provider = provider,
        providerDisplayName = "eSewa",
        amount = amount,
        currency = "NPR",
        transactionId = transactionId,
        payerName = null,
        timestamp = timestamp,
        direction = PaymentDirection.CREDIT,
        confidence = 0.9f,
    )

    // ── Transaction ID dedup ──

    @Test
    fun `same transaction ID is duplicate`() {
        val event1 = createEvent(transactionId = "TXN_001")
        val event2 = createEvent(transactionId = "TXN_001")

        assertFalse(detector.isDuplicate(event1))
        assertTrue(detector.isDuplicate(event2))
    }

    @Test
    fun `different transaction IDs are not duplicate`() {
        val event1 = createEvent(transactionId = "TXN_001")
        val event2 = createEvent(transactionId = "TXN_002")

        assertFalse(detector.isDuplicate(event1))
        assertFalse(detector.isDuplicate(event2))
    }

    // ── Two different customers, same amount ──

    @Test
    fun `two payments of same amount with different transaction IDs are NOT duplicate`() {
        val event1 = createEvent(amount = 50000, transactionId = "TXN_001")
        val event2 = createEvent(amount = 50000, transactionId = "TXN_002")

        assertFalse(detector.isDuplicate(event1))
        assertFalse(detector.isDuplicate(event2))
        // Both should be announced
    }

    // ── Fingerprint dedup (no transaction ID) ──

    @Test
    fun `same provider same amount within window without txn ID is duplicate`() {
        val now = System.currentTimeMillis()
        val event1 = createEvent(amount = 50000, timestamp = now)
        val event2 = createEvent(amount = 50000, timestamp = now + 5000) // 5 seconds later

        assertFalse(detector.isDuplicate(event1))
        assertTrue(detector.isDuplicate(event2)) // Same provider, amount, within window
    }

    @Test
    fun `same amount from different providers is NOT duplicate`() {
        val event1 = createEvent(provider = "esewa", amount = 50000)
        val event2 = createEvent(provider = "khalti", amount = 50000)

        assertFalse(detector.isDuplicate(event1))
        assertFalse(detector.isDuplicate(event2))
    }

    // ── Clear ──

    @Test
    fun `clear resets detector`() {
        val event1 = createEvent(transactionId = "TXN_001")
        assertFalse(detector.isDuplicate(event1))

        detector.clear()

        val event2 = createEvent(transactionId = "TXN_001")
        assertFalse(detector.isDuplicate(event2)) // Should not be duplicate after clear
    }
}
