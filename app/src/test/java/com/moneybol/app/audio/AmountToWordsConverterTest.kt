package com.moneybol.app.audio

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for AmountToWordsConverter.
 */
class AmountToWordsConverterTest {

    // ── Basic amounts ──

    @Test
    fun `convert 100 rupees`() {
        assertEquals("one hundred rupees", AmountToWordsConverter.convert(10000))
    }

    @Test
    fun `convert 500 rupees`() {
        assertEquals("five hundred rupees", AmountToWordsConverter.convert(50000))
    }

    @Test
    fun `convert 1000 rupees`() {
        assertEquals("one thousand rupees", AmountToWordsConverter.convert(100000))
    }

    @Test
    fun `convert 1500 rupees`() {
        assertEquals("one thousand five hundred rupees", AmountToWordsConverter.convert(150000))
    }

    @Test
    fun `convert 12450 rupees`() {
        assertEquals("twelve thousand four hundred fifty rupees", AmountToWordsConverter.convert(1245000))
    }

    // ── Edge cases ──

    @Test
    fun `convert 1 rupee`() {
        assertEquals("one rupee", AmountToWordsConverter.convert(100))
    }

    @Test
    fun `convert 0 rupees`() {
        assertEquals("zero rupees", AmountToWordsConverter.convert(0))
    }

    @Test
    fun `convert 99 rupees`() {
        assertEquals("ninety nine rupees", AmountToWordsConverter.convert(9900))
    }

    // ── South Asian numbering ──

    @Test
    fun `convert 1 lakh`() {
        assertEquals("one lakh rupees", AmountToWordsConverter.convert(10_000_000))
    }

    @Test
    fun `convert 10 lakh`() {
        assertEquals("ten lakh rupees", AmountToWordsConverter.convert(100_000_000))
    }

    @Test
    fun `convert 1 crore`() {
        assertEquals("one crore rupees", AmountToWordsConverter.convert(1_000_000_000))
    }

    // ── With paisa ──

    @Test
    fun `convert with paisa`() {
        assertEquals("five hundred rupees and fifty paisa", AmountToWordsConverter.convert(50050))
    }

    // ── Number conversion ──

    @Test
    fun `convertNumber 0`() {
        assertEquals("zero", AmountToWordsConverter.convertNumber(0))
    }

    @Test
    fun `convertNumber 15`() {
        assertEquals("fifteen", AmountToWordsConverter.convertNumber(15))
    }

    @Test
    fun `convertNumber 42`() {
        assertEquals("forty two", AmountToWordsConverter.convertNumber(42))
    }

    @Test
    fun `convertNumber 100`() {
        assertEquals("one hundred", AmountToWordsConverter.convertNumber(100))
    }

    @Test
    fun `convertNumber 999`() {
        assertEquals("nine hundred ninety nine", AmountToWordsConverter.convertNumber(999))
    }
}
