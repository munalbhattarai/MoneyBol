package com.moneybol.app.core.util

/**
 * Extracts transaction/reference identifiers from notification text.
 *
 * Supported patterns:
 * - Reference ID: XXXX
 * - Transaction ID: XXXX
 * - Ref ID: XXXX
 * - Ref No: XXXX
 * - Txn ID: XXXX
 * - Trace No: XXXX
 * - Provider-specific identifiers
 *
 * Returns null when no identifier is found — not every notification has one.
 */
object TransactionIdExtractor {

    /**
     * Patterns for extracting transaction/reference identifiers.
     * Each pattern captures the ID value in group 1.
     */
    private val ID_PATTERNS = listOf(
        // Reference ID / Ref ID / Ref No
        Regex("""(?i)(?:reference|ref)\s*(?:id|no|number|#)?[:\s=]+([A-Za-z0-9\-_]{4,30})"""),

        // Transaction ID / Txn ID / Transaction No
        Regex("""(?i)(?:transaction|txn|trans)\s*(?:id|no|number|#)?[:\s=]+([A-Za-z0-9\-_]{4,30})"""),

        // Trace No / Trace Number
        Regex("""(?i)trace\s*(?:no|number|#)?[:\s=]+([A-Za-z0-9\-_]{4,30})"""),

        // Generic ID pattern: "ID: XXXX" or "Id: XXXX"
        Regex("""(?i)\bID[:\s=]+([A-Za-z0-9\-_]{4,30})\b"""),
    )

    /**
     * Extract a transaction/reference identifier from notification text.
     *
     * @param text Notification text to search
     * @return Extracted identifier, or null if none found
     */
    fun extract(text: String?): String? {
        if (text.isNullOrBlank()) return null

        for (pattern in ID_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                val id = match.groupValues[1].trim()
                if (isValidTransactionId(id)) {
                    return id
                }
            }
        }

        return null
    }

    /**
     * Extract from multiple text sources (title, text, bigText).
     * Returns the first valid ID found.
     */
    fun extractFromNotification(
        title: String? = null,
        text: String? = null,
        bigText: String? = null
    ): String? {
        // Prefer bigText as it often has more detail
        return extract(bigText) ?: extract(text) ?: extract(title)
    }

    /**
     * Validate that the extracted string looks like a genuine transaction ID.
     */
    private fun isValidTransactionId(id: String): Boolean {
        // Must be at least 4 characters
        if (id.length < 4) return false

        // Must not be all zeros
        if (id.all { it == '0' }) return false

        // Must not look like a phone number (10 digits starting with 9)
        if (id.matches(Regex("""9\d{9}"""))) return false

        // Must not look like an OTP (4-8 pure digits)
        if (id.length in 4..8 && id.all { it.isDigit() }) {
            // Could be an OTP — only accept if longer than 8 chars
            return false
        }

        return true
    }
}
