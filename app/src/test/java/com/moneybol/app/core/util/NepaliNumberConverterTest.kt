package com.moneybol.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for NepaliNumberConverter.
 */
class NepaliNumberConverterTest {

    @Test
    fun `convert Devanagari digits to Arabic`() {
        assertEquals("0123456789", NepaliNumberConverter.toArabic("०१२३४५६७८९"))
    }

    @Test
    fun `convert single Devanagari digit`() {
        assertEquals("5", NepaliNumberConverter.toArabic("५"))
    }

    @Test
    fun `preserve non-Devanagari characters`() {
        assertEquals("Rs. 500", NepaliNumberConverter.toArabic("Rs. 500"))
    }

    @Test
    fun `convert mixed content`() {
        assertEquals("रु 500", NepaliNumberConverter.toArabic("रु ५००"))
    }

    @Test
    fun `detect Devanagari numerals`() {
        assertTrue(NepaliNumberConverter.containsDevanagariNumerals("रु ५००"))
    }

    @Test
    fun `no Devanagari numerals in Arabic text`() {
        assertFalse(NepaliNumberConverter.containsDevanagariNumerals("Rs. 500"))
    }

    @Test
    fun `convert to number`() {
        assertEquals(500L, NepaliNumberConverter.toNumber("५००"))
    }

    @Test
    fun `convert comma-separated to number`() {
        assertEquals(1500L, NepaliNumberConverter.toNumber("१,५००"))
    }

    @Test
    fun `convert Arabic digits to Devanagari`() {
        assertEquals("०१२३४५६७८९", NepaliNumberConverter.toNepali("0123456789"))
        assertEquals("५००", NepaliNumberConverter.toNepali("500"))
        assertEquals("१,५००.५०", NepaliNumberConverter.toNepali("1,500.50"))
    }
}
