package com.moneybol.app.audio

/**
 * Converts numeric amounts to Nepali words for TTS.
 *
 * Deterministic local implementation with NO external APIs or internet dependencies.
 *
 * Examples:
 *   50000 paisa (Rs. 500) → "पाँच सय रुपैयाँ"
 *   150000 paisa (Rs. 1,500) → "एक हजार पाँच सय रुपैयाँ"
 *   1000000 paisa (Rs. 10,000) → "दस हजार रुपैयाँ"
 *   150050 paisa (Rs. 1,500.50) → "एक हजार पाँच सय रुपैयाँ र पचास पैसा"
 *
 * Supports:
 *   - करोड (Crore: 10,000,000)
 *   - लाख (Lakh: 100,000)
 *   - हजार (Thousand: 1,000)
 *   - सय (Hundred: 100)
 *   - Complete 0–99 Nepali words
 *   - Paisa / decimal handling
 */
object NepaliAmountToWordsConverter {

    private val NEPALI_WORDS_0_TO_99 = arrayOf(
        "शून्य", "एक", "दुई", "तीन", "चार", "पाँच", "छ", "सात", "आठ", "नौ", "दस",
        "एघार", "बाह्र", "तेह्र", "चौध", "पन्ध्र", "सोह्र", "सत्र", "अठार", "उन्नाइस", "बीस",
        "एक्काइस", "बाइस", "तेइस", "चौबीस", "पच्चीस", "छब्बीस", "सत्ताइस", "अट्ठाइस", "उनन्तिस", "तीस",
        "एकत्तिस", "बत्तिस", "तेत्तिस", "चौँतिस", "पैँतिस", "छत्तीस", "सैँतिस", "अड्तीस", "उनन्चालीस", "चालीस",
        "एकचालीस", "बयालीस", "त्रिचालीस", "चौवालीस", "पैँतालीस", "छयालीस", "सत्चालीस", "अड्चालीस", "उनन्चास", "पचास",
        "एकाउन्न", "बाउन्न", "त्रिपन्न", "चौवन्न", "पचपन्न", "छपन्न", "सन्ताउन्न", "अन्ठाउन्न", "उनन्साठ्ठी", "साठ्ठी",
        "एकसट्ठी", "बासट्ठी", "त्रिसट्ठी", "चौंसट्ठी", "पैंसट्ठी", "छयसट्ठी", "सतसट्ठी", "अडसट्ठी", "उनन्सत्तरी", "सत्तरी",
        "एकहत्तर", "बहत्तर", "त्रिहत्तर", "चौहत्तर", "पचहत्तर", "छहत्तर", "सतहत्तर", "अठहत्तर", "उन्यासी", "असी",
        "एकासी", "बयासी", "त्रियासी", "चौरासी", "पचासी", "छयासी", "सत्तासी", "अठासी", "उनन्नब्बे", "नब्बे",
        "एकान्नब्बे", "बयानब्बे", "त्रियान्नब्बे", "चौरान्नब्बे", "पञ्चान्नब्बे", "छयान्नब्बे", "सन्तान्नब्बे", "अन्ठान्नब्बे", "उनान्सय"
    )

    /**
     * Convert amount in paisa to Nepali words with "रुपैयाँ" (and optional "पैसा") suffix.
     *
     * @param amountPaisa Amount in paisa (e.g. 50000 for Rs. 500)
     * @return Nepali speech string (e.g. "पाँच सय रुपैयाँ" or "एक हजार पाँच सय रुपैयाँ र पचास पैसा")
     */
    fun convert(amountPaisa: Long): String {
        val rupees = amountPaisa / 100
        val paisa = amountPaisa % 100

        if (rupees == 0L && paisa == 0L) return "शून्य रुपैयाँ"

        val rupeePart = if (rupees > 0) {
            "${convertNumber(rupees)} रुपैयाँ"
        } else {
            ""
        }

        val paisaPart = if (paisa > 0) {
            "${convertNumber(paisa)} पैसा"
        } else {
            ""
        }

        return when {
            rupeePart.isNotEmpty() && paisaPart.isNotEmpty() -> "$rupeePart र $paisaPart"
            rupeePart.isNotEmpty() -> rupeePart
            else -> paisaPart
        }
    }

    /**
     * Convert a non-negative number to Nepali words using South Asian numbering (करोड, लाख, हजार, सय).
     */
    fun convertNumber(number: Long): String {
        if (number == 0L) return NEPALI_WORDS_0_TO_99[0]
        if (number < 0) return "ऋण ${convertNumber(-number)}"

        val parts = mutableListOf<String>()
        var remaining = number

        // करोड (Crore = 10,000,000)
        if (remaining >= 10_000_000) {
            val crore = remaining / 10_000_000
            parts.add("${convertNumber(crore)} करोड")
            remaining %= 10_000_000
        }

        // लाख (Lakh = 100,000)
        if (remaining >= 100_000) {
            val lakh = remaining / 100_000
            parts.add("${convertNumber(lakh)} लाख")
            remaining %= 100_000
        }

        // हजार (Thousand = 1,000)
        if (remaining >= 1_000) {
            val thousand = remaining / 1_000
            parts.add("${convertNumber(thousand)} हजार")
            remaining %= 1_000
        }

        // सय (Hundred = 100)
        if (remaining >= 100) {
            val hundred = remaining / 100
            parts.add("${convertNumber(hundred)} सय")
            remaining %= 100
        }

        // 1 to 99
        if (remaining > 0) {
            parts.add(NEPALI_WORDS_0_TO_99[remaining.toInt()])
        }

        return parts.joinToString(" ")
    }
}
