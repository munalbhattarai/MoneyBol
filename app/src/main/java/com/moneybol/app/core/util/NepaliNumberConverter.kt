package com.moneybol.app.core.util

/**
 * Converts Devanagari (Nepali) numerals to Arabic numerals.
 *
 * ०१२३४५६७८९ → 0123456789
 */
object NepaliNumberConverter {

    private val DEVANAGARI_TO_ARABIC = mapOf(
        '०' to '0',
        '१' to '1',
        '२' to '2',
        '३' to '3',
        '४' to '4',
        '५' to '5',
        '६' to '6',
        '७' to '7',
        '८' to '8',
        '९' to '9'
    )

    private val ARABIC_TO_DEVANAGARI = mapOf(
        '0' to '०',
        '1' to '१',
        '2' to '२',
        '3' to '३',
        '4' to '४',
        '5' to '५',
        '6' to '६',
        '7' to '७',
        '8' to '८',
        '9' to '९'
    )

    /**
     * Convert a string containing Devanagari numerals to Arabic numerals.
     * Non-Devanagari characters are preserved as-is.
     */
    fun toArabic(input: String): String {
        return input.map { char ->
            DEVANAGARI_TO_ARABIC[char] ?: char
        }.joinToString("")
    }

    /**
     * Convert a string containing Arabic numerals to Devanagari numerals.
     * Non-Arabic characters are preserved as-is.
     */
    fun toNepali(input: String): String {
        return input.map { char ->
            ARABIC_TO_DEVANAGARI[char] ?: char
        }.joinToString("")
    }

    /**
     * Check if the string contains any Devanagari numerals.
     */
    fun containsDevanagariNumerals(input: String): Boolean {
        return input.any { it in DEVANAGARI_TO_ARABIC }
    }

    /**
     * Convert a string with Devanagari numerals to a Long value.
     * Returns null if the string cannot be parsed.
     */
    fun toNumber(input: String): Long? {
        val arabicString = toArabic(input).replace(",", "").replace(" ", "")
        return arabicString.toLongOrNull()
    }
}
