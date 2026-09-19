package com.moneybol.app.audio

/**
 * Converts numeric amounts to English words for TTS.
 *
 * Examples:
 *   500 → "five hundred"
 *   1500 → "one thousand five hundred"
 *   12450 → "twelve thousand four hundred fifty"
 *   100 → "one hundred"
 *   1 → "one"
 *   0 → "zero"
 *   1000000 → "ten lakh" (using South Asian numbering)
 *
 * Amount input is in RUPEES (not paisa).
 */
object AmountToWordsConverter {

    private val ONES = arrayOf(
        "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen"
    )

    private val TENS = arrayOf(
        "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
    )

    /**
     * Convert amount in paisa to words with "rupees" suffix.
     *
     * @param amountPaisa Amount in paisa (50000 for Rs. 500)
     * @return "five hundred rupees" or "one thousand five hundred rupees and fifty paisa"
     */
    fun convert(amountPaisa: Long): String {
        val rupees = amountPaisa / 100
        val paisa = amountPaisa % 100

        if (rupees == 0L && paisa == 0L) return "zero rupees"

        val rupeePart = if (rupees > 0) {
            "${convertNumber(rupees)} ${if (rupees == 1L) "rupee" else "rupees"}"
        } else {
            ""
        }

        val paisaPart = if (paisa > 0) {
            "${convertNumber(paisa)} paisa"
        } else {
            ""
        }

        return when {
            rupeePart.isNotEmpty() && paisaPart.isNotEmpty() -> "$rupeePart and $paisaPart"
            rupeePart.isNotEmpty() -> rupeePart
            else -> paisaPart
        }
    }

    /**
     * Convert a number to South Asian English words.
     * Uses lakh/crore system common in Nepal/India.
     */
    fun convertNumber(number: Long): String {
        if (number == 0L) return "zero"
        if (number < 0) return "minus ${convertNumber(-number)}"

        val parts = mutableListOf<String>()

        var remaining = number

        // Crore (10 million)
        if (remaining >= 10_000_000) {
            val crore = remaining / 10_000_000
            parts.add("${convertNumber(crore)} crore")
            remaining %= 10_000_000
        }

        // Lakh (100 thousand)
        if (remaining >= 100_000) {
            val lakh = remaining / 100_000
            parts.add("${convertNumber(lakh)} lakh")
            remaining %= 100_000
        }

        // Thousand
        if (remaining >= 1_000) {
            val thousand = remaining / 1_000
            parts.add("${convertNumber(thousand)} thousand")
            remaining %= 1_000
        }

        // Hundred
        if (remaining >= 100) {
            val hundred = remaining / 100
            parts.add("${ONES[hundred.toInt()]} hundred")
            remaining %= 100
        }

        // Tens and ones
        if (remaining > 0) {
            if (remaining < 20) {
                parts.add(ONES[remaining.toInt()])
            } else {
                val ten = remaining / 10
                val one = remaining % 10
                val tenWord = TENS[ten.toInt()]
                if (one > 0) {
                    parts.add("$tenWord ${ONES[one.toInt()]}")
                } else {
                    parts.add(tenWord)
                }
            }
        }

        return parts.joinToString(" ")
    }
}
