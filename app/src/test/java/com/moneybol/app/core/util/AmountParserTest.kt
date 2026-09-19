package com.moneybol.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for AmountParser.
 *
 * Amounts are returned in PAISA (1 rupee = 100 paisa).
 */
class AmountParserTest {

    // ── Basic Rs. formats ──

    @Test
    fun `parse Rs 500`() {
        assertEquals(50000L, AmountParser.parse("Rs. 500"))
    }

    @Test
    fun `parse Rs 500 without dot`() {
        assertEquals(50000L, AmountParser.parse("Rs 500"))
    }

    @Test
    fun `parse Rs500 no space`() {
        assertEquals(50000L, AmountParser.parse("Rs.500"))
    }

    @Test
    fun `parse Rs with comma separated thousands`() {
        assertEquals(150000L, AmountParser.parse("Rs. 1,500"))
    }

    @Test
    fun `parse Rs large amount`() {
        assertEquals(1245000L, AmountParser.parse("Rs. 12,450"))
    }

    @Test
    fun `parse Rs with decimal`() {
        assertEquals(150050L, AmountParser.parse("Rs. 1,500.50"))
    }

    // ── NPR formats ──

    @Test
    fun `parse NPR 500`() {
        assertEquals(50000L, AmountParser.parse("NPR 500"))
    }

    @Test
    fun `parse NPR 1500`() {
        assertEquals(150000L, AmountParser.parse("NPR 1,500"))
    }

    // ── Devanagari formats ──

    @Test
    fun `parse Devanagari rupee symbol with Devanagari numerals`() {
        assertEquals(50000L, AmountParser.parse("रु ५००"))
    }

    @Test
    fun `parse Devanagari with dot`() {
        assertEquals(50000L, AmountParser.parse("रु. ५००"))
    }

    @Test
    fun `parse Devanagari rupee symbol with Arabic numerals`() {
        assertEquals(50000L, AmountParser.parse("रु 500"))
    }

    // ── Amount in context ──

    @Test
    fun `parse amount from bank notification`() {
        val text = "Your account has been credited by Rs. 1,500 on 2024-01-15"
        assertEquals(150000L, AmountParser.parse(text))
    }

    @Test
    fun `parse amount from simple notification`() {
        val text = "Payment received Rs. 500"
        assertEquals(50000L, AmountParser.parse(text))
    }

    // ── Edge cases ──

    @Test
    fun `return null for empty string`() {
        assertNull(AmountParser.parse(""))
    }

    @Test
    fun `return null for null`() {
        assertNull(AmountParser.parse(null))
    }

    @Test
    fun `return null for text without amount`() {
        assertNull(AmountParser.parse("Welcome to MoneyBol"))
    }

    @Test
    fun `do not parse phone number as amount`() {
        // 9840000000 is a phone number, not an amount
        // The parser should not match it since there's no Rs/NPR prefix
        val result = AmountParser.parse("Call 9840000000 for support")
        assertNull(result)
    }

    @Test
    fun `parse minimum valid amount`() {
        assertEquals(100L, AmountParser.parse("Rs. 1"))
    }

    // ── Format for display ──

    @Test
    fun `format display whole rupees`() {
        assertEquals("500", AmountParser.formatForDisplay(50000L))
    }

    @Test
    fun `format display with comma`() {
        assertEquals("1,500", AmountParser.formatForDisplay(150000L))
    }

    @Test
    fun `format display with paisa`() {
        assertEquals("1,500.50", AmountParser.formatForDisplay(150050L))
    }
}
