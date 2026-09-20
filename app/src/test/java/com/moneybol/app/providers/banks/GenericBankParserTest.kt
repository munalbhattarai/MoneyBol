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

    // ── Real Nepali Bank SMS Samples ──

    @Test
    fun `parse Machhapuchchhre Bank MBL Fonepay QR merchant SMS alert`() {
        val notification = createNotification(
            title = "MBL_ALERT",
            text = "Dear Merchant,\nFP QR transaction from 984#077 of NPR 100.00 is successful.\nRRN: 44802649c2RB\nThank you\n- MBL\nDownload: onelink.to/fp9e4k",
        )

        assertTrue(parser.canHandle(notification))
        val event = parser.parse(notification)

        assertNotNull(event)
        assertEquals(10000L, event?.amount) // NPR 100.00 -> 10000 paisa
        assertEquals(PaymentDirection.CREDIT, event?.direction)
        assertEquals("44802649c2RB", event?.transactionId)
        assertEquals("Machhapuchchhre Bank", event?.providerDisplayName)
        assertEquals("984#077", event?.payerName)
        assertTrue((event?.confidence ?: 0f) >= 0.7f)
    }

    @Test
    fun `parse NIC ASIA Bank credited alert with Remarks`() {
        val notification = createNotification(
            title = "NICA_ALERT",
            text = "Your 226###41002 has been Credited by NPR 2,510.00 on 20/09/2026 11:05:46, Remarks: FPQR-485270400-5865-32. Survey: https://bit.ly/42utwDR ,Help us improve!. NI",
        )

        assertTrue(parser.canHandle(notification))
        val event = parser.parse(notification)

        assertNotNull(event)
        assertEquals(251000L, event?.amount) // NPR 2,510.00 -> 251000 paisa
        assertEquals(PaymentDirection.CREDIT, event?.direction)
        assertEquals("FPQR-485270400-5865-32", event?.transactionId)
        assertEquals("NIC ASIA Bank", event?.providerDisplayName)
        assertTrue((event?.confidence ?: 0f) >= 0.7f)
    }

    @Test
    fun `parse NIC ASIA Bank received from alert with RRN`() {
        val notification = createNotification(
            title = "NICA_ALERT",
            text = "Rs. 10.00 received from 982****191 for RRN: 14240939JDsX on 2026-09-20 03:26:23 Download MoBank- http://bit.ly/MoBank4 Thank You NIC ASIA BANK",
        )

        assertTrue(parser.canHandle(notification))
        val event = parser.parse(notification)

        assertNotNull(event)
        assertEquals(1000L, event?.amount) // Rs. 10.00 -> 1000 paisa
        assertEquals(PaymentDirection.CREDIT, event?.direction)
        assertEquals("14240939JDsX", event?.transactionId)
        assertEquals("NIC ASIA Bank", event?.providerDisplayName)
        assertEquals("982****191", event?.payerName)
        assertTrue((event?.confidence ?: 0f) >= 0.7f)
    }

    @Test
    fun `parse Nabil Bank SMS alert`() {
        val notification = createNotification(
            title = "NABIL_ALERT",
            text = "Your A/C 01234XXXX has been credited with NPR 500.00 on 20/09/2026. Ref: NABIL887766. Available Bal: NPR 15,000.00",
        )

        assertTrue(parser.canHandle(notification))
        val event = parser.parse(notification)

        assertNotNull(event)
        assertEquals(50000L, event?.amount)
        assertEquals(PaymentDirection.CREDIT, event?.direction)
        assertEquals("NABIL887766", event?.transactionId)
        assertEquals("Nabil Bank", event?.providerDisplayName)
        assertTrue((event?.confidence ?: 0f) >= 0.7f)
    }

    @Test
    fun `parse Global IME Bank SMS alert`() {
        val notification = createNotification(
            title = "GBIME_ALERT",
            text = "Your account has been credited by NPR 1,200.00 on 20/09/2026. Remarks: FPQR-554433. Global IME Bank",
        )

        assertTrue(parser.canHandle(notification))
        val event = parser.parse(notification)

        assertNotNull(event)
        assertEquals(120000L, event?.amount)
        assertEquals(PaymentDirection.CREDIT, event?.direction)
        assertEquals("FPQR-554433", event?.transactionId)
        assertEquals("Global IME Bank", event?.providerDisplayName)
        assertTrue((event?.confidence ?: 0f) >= 0.7f)
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
