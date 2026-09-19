package com.moneybol.app.providers.banks

import com.moneybol.app.core.model.PaymentDirection
import com.moneybol.app.core.model.RawNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for GenericBankParser.
 */
class GenericBankParserTest {

    private lateinit var parser: GenericBankParser

    @Before
    fun setUp() {
        parser = GenericBankParser()
    }

    private fun createNotification(
        text: String,
        title: String = "Bank Alert",
        packageName: String = "com.google.android.apps.messaging",
    ) = RawNotification(
        packageName = packageName,
        appName = "Messages",
        title = title,
        text = text,
        bigText = null,
        timestamp = System.currentTimeMillis(),
    )

    // ── Incoming credit patterns ──

    @Test
    fun `parse incoming credit with Rs format and transaction ID`() {
        val notification = createNotification(
            text = "Your account XXXXX1234 has been credited by Rs. 1,500 on 2024-01-15. Txn ID: TXN98765. Available Bal: Rs. 25,000."
        )

        assertTrue(parser.canHandle(notification))
        val event = parser.parse(notification)

        assertNotNull(event)
        assertEquals(150000L, event?.amount) // 1500 * 100
        assertEquals(PaymentDirection.CREDIT, event?.direction)
        assertEquals("TXN98765", event?.transactionId)
    }

    @Test
    fun `parse incoming credit with NPR format`() {
        val notification = createNotification(
            text = "NPR 2,500 deposited to your account XXXXX5678. Ref No: REF123456. Bal: NPR 30,000."
        )

        assertTrue(parser.canHandle(notification))
        val event = parser.parse(notification)

        assertNotNull(event)
        assertEquals(250000L, event?.amount)
        assertEquals(PaymentDirection.CREDIT, event?.direction)
        assertEquals("REF123456", event?.transactionId)
    }

    @Test
    fun `parse incoming credit with Devanagari Rupee and numerals`() {
        val notification = createNotification(
            text = "तपाईंको खातामा रु. ५०० जम्मा भएको छ। Ref ID: BANK12345"
        )

        assertTrue(parser.canHandle(notification))
        val event = parser.parse(notification)

        assertNotNull(event)
        assertEquals(50000L, event?.amount)
        assertEquals(PaymentDirection.CREDIT, event?.direction)
    }

    // ── Safety Rejections ──

    @Test
    fun `reject debit notification`() {
        val notification = createNotification(
            text = "Your account XXXXX1234 has been debited by Rs. 1,500 for ATM withdrawal. Txn ID: TXN98765."
        )

        assertFalse(parser.canHandle(notification))
        assertNull(parser.parse(notification))
    }

    @Test
    fun `reject OTP notification with amount`() {
        val notification = createNotification(
            text = "Your OTP for Rs. 1,500 transfer to account XXXXX1234 is 456789. Do not share."
        )

        assertFalse(parser.canHandle(notification))
        assertNull(parser.parse(notification))
    }

    @Test
    fun `reject reversed transaction notification`() {
        val notification = createNotification(
            text = "Transaction of Rs. 1,500 has been reversed for account XXXXX1234. Ref: REV9876."
        )

        assertFalse(parser.canHandle(notification))
        assertNull(parser.parse(notification))
    }

    @Test
    fun `reject pending transaction notification`() {
        val notification = createNotification(
            text = "Your deposit of Rs. 1,500 to account XXXXX1234 is pending verification."
        )

        assertFalse(parser.canHandle(notification))
        assertNull(parser.parse(notification))
    }

    @Test
    fun `reject ambiguous notification without clear credit keyword`() {
        val notification = createNotification(
            text = "Transaction alert: NPR 500 at Store ABC on 2024-01-15."
        )

        assertFalse(parser.canHandle(notification))
        assertNull(parser.parse(notification))
    }

    @Test
    fun `reject notification with missing amount`() {
        val notification = createNotification(
            text = "Your account XXXXX1234 has been credited. Check your balance."
        )

        assertFalse(parser.canHandle(notification))
        assertNull(parser.parse(notification))
    }
}
