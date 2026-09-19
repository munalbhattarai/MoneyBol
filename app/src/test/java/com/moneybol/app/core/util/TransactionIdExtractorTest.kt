package com.moneybol.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for TransactionIdExtractor.
 */
class TransactionIdExtractorTest {

    @Test
    fun `extract Reference ID`() {
        val id = TransactionIdExtractor.extract("Reference ID: TXN123456789")
        assertEquals("TXN123456789", id)
    }

    @Test
    fun `extract Ref No`() {
        val id = TransactionIdExtractor.extract("Ref No: REF-98765")
        assertEquals("REF-98765", id)
    }

    @Test
    fun `extract Transaction ID`() {
        val id = TransactionIdExtractor.extract("Transaction ID: ABCD12345678")
        assertEquals("ABCD12345678", id)
    }

    @Test
    fun `extract Txn ID`() {
        val id = TransactionIdExtractor.extract("Txn ID: TX_2024_001")
        assertEquals("TX_2024_001", id)
    }

    @Test
    fun `return null for no ID`() {
        val id = TransactionIdExtractor.extract("Payment received Rs. 500")
        assertNull(id)
    }

    @Test
    fun `return null for empty string`() {
        assertNull(TransactionIdExtractor.extract(""))
    }

    @Test
    fun `return null for null`() {
        assertNull(TransactionIdExtractor.extract(null))
    }

    @Test
    fun `do not extract OTP as transaction ID`() {
        // Short pure-digit sequences could be OTPs — reject them
        val id = TransactionIdExtractor.extract("ID: 123456")
        assertNull(id) // 6-digit pure number rejected (could be OTP)
    }

    @Test
    fun `extract from multiple sources prefers bigText`() {
        val id = TransactionIdExtractor.extractFromNotification(
            title = null,
            text = "Payment received",
            bigText = "Payment received Rs. 500. Ref ID: BIGTEXT_001",
        )
        assertEquals("BIGTEXT_001", id)
    }
}
