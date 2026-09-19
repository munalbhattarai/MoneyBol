package com.moneybol.app.core.util

/**
 * Parses monetary amounts from notification text.
 *
 * Supports:
 * - Rs. 500, Rs 500, Rs.500
 * - NPR 500, NPR 1,500, NPR 1,500.50
 * - रु 500, रु. 500, रु ५००, रु. ५००
 * - Amount patterns near currency indicators
 *
 * Returns amount in PAISA (1 rupee = 100 paisa) for precision.
 * Rs. 500 → 50000
 * Rs. 1,500.50 → 150050
 *
 * SAFETY: Must NOT accidentally parse account numbers, reference numbers,
 * phone numbers, OTPs, or dates as payment amounts.
 */
object AmountParser {

    /**
     * Regex patterns for amount extraction.
     * Ordered by specificity — more specific patterns first.
     */
    private val AMOUNT_PATTERNS = listOf(
        // Rs. 1,500.50 or Rs 1,500.50 (with optional dot after Rs)
        Regex("""(?i)\bRs\.?\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)\b"""),

        // NPR 1,500.50
        Regex("""(?i)\bNPR\.?\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)\b"""),

        // रु. ५०० or रु ५०० (Devanagari currency + Devanagari numerals)
        Regex("""रु\.?\s*([०-९]{1,3}(?:,[०-९]{3})*(?:\.[०-९]{1,2})?)"""),

        // रु. 500 or रु 500 (Devanagari currency + Arabic numerals)
        Regex("""रु\.?\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)"""),

        // Amount followed by "rupees" (English)
        Regex("""(?i)\b([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)\s*rupees?\b"""),
    )

    /**
     * Patterns that look like amounts but are NOT payment amounts.
     * Used to filter out false positives.
     */
    private val EXCLUSION_CONTEXT_PATTERNS = listOf(
        // Phone numbers: 10-digit numbers starting with 9
        Regex("""(?<!\d)9[0-9]{9}(?!\d)"""),
        // Account numbers: long digit sequences (12+ digits)
        Regex("""(?<!\d)[0-9]{12,}(?!\d)"""),
        // OTP patterns
        Regex("""(?i)\bOTP\s*[:=]?\s*[0-9]{4,8}\b"""),
        // Date patterns like 2024/01/15 or 15-01-2024
        Regex("""(?<!\d)\d{4}[/\-]\d{2}[/\-]\d{2}(?!\d)"""),
        Regex("""(?<!\d)\d{2}[/\-]\d{2}[/\-]\d{4}(?!\d)"""),
    )

    /**
     * Parse the payment amount from notification text.
     *
     * @param text The notification text to parse
     * @return Amount in paisa, or null if no valid amount found
     */
    fun parse(text: String?): Long? {
        if (text.isNullOrBlank()) return null

        // Try to convert any Devanagari numerals to Arabic for unified processing
        val normalizedText = NepaliNumberConverter.toArabic(text)

        for (pattern in AMOUNT_PATTERNS) {
            val matchResult = pattern.find(text) ?: pattern.find(normalizedText)
            if (matchResult != null) {
                val amountString = matchResult.groupValues[1]
                val parsedAmount = parseAmountString(
                    NepaliNumberConverter.toArabic(amountString)
                )
                if (parsedAmount != null && isValidAmount(parsedAmount, matchResult, text)) {
                    return parsedAmount
                }
            }
        }

        return null
    }

    /**
     * Parse all amounts found in the text.
     * Useful for validation — if multiple amounts are found, the parser
     * may need additional context to determine which is the transaction amount.
     */
    fun parseAll(text: String?): List<Long> {
        if (text.isNullOrBlank()) return emptyList()

        val normalizedText = NepaliNumberConverter.toArabic(text)
        val amounts = mutableListOf<Long>()

        for (pattern in AMOUNT_PATTERNS) {
            val matches = pattern.findAll(text) + pattern.findAll(normalizedText)
            for (matchResult in matches) {
                val amountString = matchResult.groupValues[1]
                val parsedAmount = parseAmountString(
                    NepaliNumberConverter.toArabic(amountString)
                )
                if (parsedAmount != null && isValidAmount(parsedAmount, matchResult, text)) {
                    if (parsedAmount !in amounts) {
                        amounts.add(parsedAmount)
                    }
                }
            }
        }

        return amounts
    }

    /**
     * Parse a cleaned amount string to paisa.
     * "1,500.50" → 150050
     * "500" → 50000
     */
    private fun parseAmountString(amountStr: String): Long? {
        val cleaned = amountStr.replace(",", "").trim()
        if (cleaned.isEmpty()) return null

        return try {
            if (cleaned.contains(".")) {
                val parts = cleaned.split(".")
                val rupees = parts[0].toLongOrNull() ?: return null
                val paisaStr = parts.getOrElse(1) { "0" }.take(2).padEnd(2, '0')
                val paisa = paisaStr.toLongOrNull() ?: return null
                rupees * 100 + paisa
            } else {
                val rupees = cleaned.toLongOrNull() ?: return null
                rupees * 100
            }
        } catch (_: NumberFormatException) {
            null
        }
    }

    /**
     * Validate that a parsed amount is reasonable and not a false positive.
     */
    private fun isValidAmount(amountPaisa: Long, match: MatchResult, originalText: String): Boolean {
        // Amount must be positive
        if (amountPaisa <= 0) return false

        // Amount must be at least 1 rupee (100 paisa)
        if (amountPaisa < 100) return false

        // Sanity check: unlikely to be a single payment above 10 crore (100 million rupees)
        if (amountPaisa > 10_000_000_000L) return false

        // Check that the matched region doesn't overlap with an exclusion pattern
        val matchRange = match.range
        for (exclusion in EXCLUSION_CONTEXT_PATTERNS) {
            val exclusionMatches = exclusion.findAll(originalText)
            for (exclusionMatch in exclusionMatches) {
                if (matchRange.first >= exclusionMatch.range.first &&
                    matchRange.last <= exclusionMatch.range.last
                ) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Format an amount in paisa to a display string.
     * 50000 → "500"
     * 150050 → "1,500.50"
     */
    fun formatForDisplay(amountPaisa: Long): String {
        val rupees = amountPaisa / 100
        val paisa = amountPaisa % 100
        return if (paisa == 0L) {
            "%,d".format(rupees)
        } else {
            "%,.2f".format(amountPaisa / 100.0)
        }
    }
}
