package com.moneybol.app.audio

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for NepaliAmountToWordsConverter.
 */
class NepaliAmountToWordsConverterTest {

    @Test
    fun `convert 500 rupees to Nepali words`() {
        // Rs. 500 = 50000 paisa
        val result = NepaliAmountToWordsConverter.convert(50_000L)
        assertEquals("पाँच सय रुपैयाँ", result)
    }

    @Test
    fun `convert 1500 rupees to Nepali words`() {
        // Rs. 1,500 = 150000 paisa
        val result = NepaliAmountToWordsConverter.convert(150_000L)
        assertEquals("एक हजार पाँच सय रुपैयाँ", result)
    }

    @Test
    fun `convert 10000 rupees to Nepali words`() {
        // Rs. 10,000 = 1000000 paisa
        val result = NepaliAmountToWordsConverter.convert(1_000_000L)
        assertEquals("दस हजार रुपैयाँ", result)
    }

    @Test
    fun `convert 1 lakh rupees to Nepali words`() {
        // Rs. 100,000 = 10000000 paisa
        val result = NepaliAmountToWordsConverter.convert(10_000_000L)
        assertEquals("एक लाख रुपैयाँ", result)
    }

    @Test
    fun `convert 1 crore rupees to Nepali words`() {
        // Rs. 10,000,000 = 1000000000 paisa
        val result = NepaliAmountToWordsConverter.convert(1_000_000_000L)
        assertEquals("एक करोड रुपैयाँ", result)
    }

    @Test
    fun `convert amount with paisa decimal`() {
        // Rs. 1,500.50 = 150050 paisa
        val result = NepaliAmountToWordsConverter.convert(150_050L)
        assertEquals("एक हजार पाँच सय रुपैयाँ र पचास पैसा", result)
    }

    @Test
    fun `convert only paisa`() {
        // 75 paisa
        val result = NepaliAmountToWordsConverter.convert(75L)
        assertEquals("पचहत्तर पैसा", result)
    }

    @Test
    fun `convert zero`() {
        val result = NepaliAmountToWordsConverter.convert(0L)
        assertEquals("शून्य रुपैयाँ", result)
    }

    @Test
    fun `convert specific numbers 1 to 20`() {
        assertEquals("एक", NepaliAmountToWordsConverter.convertNumber(1L))
        assertEquals("दुई", NepaliAmountToWordsConverter.convertNumber(2L))
        assertEquals("तीन", NepaliAmountToWordsConverter.convertNumber(3L))
        assertEquals("चार", NepaliAmountToWordsConverter.convertNumber(4L))
        assertEquals("पाँच", NepaliAmountToWordsConverter.convertNumber(5L))
        assertEquals("दस", NepaliAmountToWordsConverter.convertNumber(10L))
        assertEquals("एघार", NepaliAmountToWordsConverter.convertNumber(11L))
        assertEquals("पन्ध्र", NepaliAmountToWordsConverter.convertNumber(15L))
        assertEquals("बीस", NepaliAmountToWordsConverter.convertNumber(20L))
    }

    @Test
    fun `convert compound thousands and hundreds`() {
        // 25,340
        assertEquals("पच्चीस हजार तीन सय चालीस", NepaliAmountToWordsConverter.convertNumber(25_340L))
        // 99,999
        assertEquals("उनान्सय हजार नौ सय उनान्सय", NepaliAmountToWordsConverter.convertNumber(99_999L))
    }
}
