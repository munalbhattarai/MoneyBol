package com.moneybol.app.providers.banks

import com.moneybol.app.core.model.PaymentDirection
import com.moneybol.app.core.model.PaymentEvent
import com.moneybol.app.core.model.RawNotification
import com.moneybol.app.core.util.AmountParser
import com.moneybol.app.core.util.DirectionDetector
import com.moneybol.app.core.util.TransactionIdExtractor
import com.moneybol.app.providers.PaymentNotificationParser
import java.util.UUID

/**
 * Generic bank notification parser.
 *
 * Handles incoming payment notifications and SMS alerts from all Nepali banks,
 * payment networks (Fonepay, NepalPay, Smart QR), and mobile banking apps.
 *
 * Supports:
 * - SMS alerts from bank shortcodes / sender IDs (e.g. MBL_ALERT, NICA_ALERT, NABIL_ALERT)
 * - Fonepay Merchant QR transaction alerts ("FP QR transaction from ... is successful")
 * - Account credit alerts ("has been Credited by NPR ...", "credited with Rs. ...")
 * - Direct received alerts ("Rs. 10.00 received from ... for RRN: ...")
 * - Push notifications from mobile banking applications
 *
 * Conservative CREDIT-only detection with strict duplicate and safety protections.
 */
class GenericBankParser : PaymentNotificationParser {

    override val providerId = "generic_bank"
    override val providerDisplayName = "Bank"

    override val supportedPackageNames: Set<String> = emptySet()

    /**
     * Known SMS app package names across Android manufacturers.
     */
    private val smsPackages = setOf(
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms",
        "com.xiaomi.mms",
        "com.oneplus.mms",
        "com.coloros.mms",
        "com.heytap.mms",
        "com.vivo.mms",
        "com.motorola.mms",
        "com.truecaller",
        "com.microsoft.android.smsorganizer",
        "com.simplemobiletools.smsmessenger",
    )

    /**
     * Known Nepali bank app package name prefixes.
     */
    private val bankPackagePrefixes = setOf(
        "com.nabilbank",
        "com.nmbmobilebanking",
        "com.gibl",
        "com.hbl",
        "com.siddharthabank",
        "com.nepalbank",
        "com.nmb",
        "com.prabhubank",
        "com.mbl",
        "com.sanima",
        "com.everestbank",
        "com.kumari",
        "com.sbi",
        "com.rbb",
        "com.adbl",
        "com.scb",
        "com.citizens",
        "com.prime",
        "com.laxmi",
        "com.fonepay",
        "com.nchl",
    )

    private data class BankDefinition(
        val providerId: String,
        val displayName: String,
        val keywords: List<String>,
    )

    private val knownBanks = listOf(
        BankDefinition("bank_nic_asia", "NIC ASIA Bank", listOf("nica", "nic asia", "mobank")),
        BankDefinition("bank_machhapuchchhre", "Machhapuchchhre Bank", listOf("mbl", "machhapuchchhre")),
        BankDefinition("bank_nabil", "Nabil Bank", listOf("nabil")),
        BankDefinition("bank_global_ime", "Global IME Bank", listOf("gbime", "global ime")),
        BankDefinition("bank_himalayan", "Himalayan Bank", listOf("hbl", "himalayan")),
        BankDefinition("bank_sanima", "Sanima Bank", listOf("sanima")),
        BankDefinition("bank_everest", "Everest Bank", listOf("ebl", "everest")),
        BankDefinition("bank_prabhu", "Prabhu Bank", listOf("prabhu")),
        BankDefinition("bank_siddhartha", "Siddhartha Bank", listOf("sbl", "siddhartha")),
        BankDefinition("bank_nmb", "NMB Bank", listOf("nmb")),
        BankDefinition("bank_kumari", "Kumari Bank", listOf("kbl", "kumari")),
        BankDefinition("bank_sbi", "Nepal SBI Bank", listOf("sbi", "nsbl", "nepal sbi")),
        BankDefinition("bank_nepal_bank", "Nepal Bank", listOf("nbl", "nepal bank")),
        BankDefinition("bank_rbb", "Rastriya Banijya Bank", listOf("rbb", "rastriya banijya")),
        BankDefinition("bank_adbl", "Agricultural Development Bank", listOf("adbl", "agriculture")),
        BankDefinition("bank_scb", "Standard Chartered Bank", listOf("scb", "standard chartered")),
        BankDefinition("bank_citizens", "Citizens Bank", listOf("czn", "citizens")),
        BankDefinition("bank_prime", "Prime Commercial Bank", listOf("pcbl", "prime")),
        BankDefinition("bank_laxmi", "Laxmi Sunrise Bank", listOf("lbl", "laxmi")),
        BankDefinition("fonepay", "Fonepay", listOf("fonepay", "fp qr", "fp_alert")),
        BankDefinition("nepalpay", "NepalPay", listOf("nepalpay", "nchl")),
    )

    override fun canHandle(notification: RawNotification): Boolean {
        val text = getCombinedText(notification)
        if (text.isBlank()) return false

        // Must have an amount
        val amount = AmountParser.parse(text) ?: return false

        // Must have a clear credit indicator
        val direction = DirectionDetector.detect(
            title = notification.title,
            text = notification.text,
            bigText = notification.bigText,
        )

        // Only handle clear credit cases
        if (direction != PaymentDirection.CREDIT) return false

        // Must be bank or payment related
        return isBankRelated(notification)
    }

    override fun parse(notification: RawNotification): PaymentEvent? {
        val text = getCombinedText(notification)
        if (text.isBlank()) return null

        val amount = AmountParser.parse(text) ?: return null

        val direction = DirectionDetector.detect(
            title = notification.title,
            text = notification.text,
            bigText = notification.bigText,
        )

        if (direction != PaymentDirection.CREDIT) return null
        if (!isBankRelated(notification)) return null

        val transactionId = TransactionIdExtractor.extractFromNotification(
            title = notification.title,
            text = notification.text,
            bigText = notification.bigText,
        )

        val (bankId, bankDisplayName) = resolveBank(notification)
        val isSpecificBank = bankId != "bank_generic"

        val confidence = calculateConfidence(notification, amount, transactionId, isSpecificBank)
        if (confidence < 0.7f) return null

        val payer = extractPayer(text)

        return PaymentEvent(
            id = UUID.randomUUID().toString(),
            provider = bankId,
            providerDisplayName = bankDisplayName,
            amount = amount,
            currency = "NPR",
            transactionId = transactionId,
            payerName = payer,
            timestamp = notification.timestamp,
            direction = direction,
            confidence = confidence,
        )
    }

    private fun getCombinedText(notification: RawNotification): String {
        return buildString {
            notification.title?.let { append(it); append(" ") }
            notification.text?.let { append(it); append(" ") }
            notification.bigText?.let { append(it) }
        }
    }

    private fun isBankRelated(notification: RawNotification): Boolean {
        val pkg = notification.packageName.lowercase()

        // 1. Known SMS apps
        if (smsPackages.any { pkg.startsWith(it) }) return true

        // 2. Known banking app packages
        if (bankPackagePrefixes.any { pkg.startsWith(it) } ||
            pkg.contains(".bank") || pkg.contains(".mobilebanking") || pkg.contains(".mbank")
        ) return true

        // 3. Sender title indicates bank alert
        val title = (notification.title ?: "").lowercase()
        if (title.contains("_alert") || title.contains("alert") ||
            title.contains("bank") || title.contains("fonepay") || title.contains("nepalpay")
        ) return true

        // 4. Content indicates bank or payment network transaction
        val text = getCombinedText(notification).lowercase()
        if (text.contains("a/c") || text.contains("account") ||
            text.contains("rrn:") || text.contains("fp qr") || text.contains("qr transaction") ||
            text.contains("fonepay") || text.contains("nepalpay") || text.contains("remarks:") ||
            text.contains("dear merchant") || text.contains("bank")
        ) return true

        return false
    }

    private fun resolveBank(notification: RawNotification): Pair<String, String> {
        val title = (notification.title ?: "").lowercase()
        val text = getCombinedText(notification).lowercase()
        val appName = (notification.appName ?: "").lowercase()

        // 1. Match from title (SMS sender like MBL_ALERT, NICA_ALERT)
        val cleanTitle = title.replace("_alert", "").replace("alert", "").trim()
        for (bank in knownBanks) {
            if (bank.keywords.any { cleanTitle.contains(it) }) {
                return Pair(bank.providerId, bank.displayName)
            }
        }

        // 2. Match from combined text (e.g. "- MBL", "NIC ASIA BANK", "Thank you NIC ASIA")
        for (bank in knownBanks) {
            if (bank.keywords.any { text.contains(it) }) {
                return Pair(bank.providerId, bank.displayName)
            }
        }

        // 3. Match from appName (if a banking app is installed)
        for (bank in knownBanks) {
            if (bank.keywords.any { appName.contains(it) }) {
                return Pair(bank.providerId, bank.displayName)
            }
        }

        // 4. Check if title ends with _ALERT (e.g. XYZ_ALERT)
        val alertRegex = Regex("""(?i)([A-Za-z0-9]{2,8})_alert""")
        val match = alertRegex.find(notification.title ?: "")
        if (match != null) {
            val code = match.groupValues[1].uppercase()
            return Pair("bank_${code.lowercase()}", "$code Bank")
        }

        // 5. Fallback
        return Pair("bank_generic", "Bank")
    }

    private fun extractPayer(text: String): String? {
        val payerRegex = Regex(
            """(?i)(?:received|transaction|payment|fund|money)?\s*from\s+([0-9#*A-Za-z\s]{3,25}?)(?:\s+(?:of|for|on|in|at|to|with|via)\b|[,.\n]|$)"""
        )
        val match = payerRegex.find(text) ?: return null
        val candidate = match.groupValues[1].trim()
        if (candidate.startsWith("NPR", ignoreCase = true) ||
            candidate.startsWith("Rs", ignoreCase = true) ||
            candidate.length < 3
        ) return null
        return candidate
    }

    private fun calculateConfidence(
        notification: RawNotification,
        amount: Long,
        transactionId: String?,
        isSpecificBank: Boolean
    ): Float {
        var confidence = 0.5f

        // Verified CREDIT direction
        confidence += 0.2f // 0.70f

        // Is from known bank/SMS package or bank sender
        if (isBankRelated(notification)) confidence += 0.1f // 0.80f

        // Identified specific bank or payment network
        if (isSpecificBank) confidence += 0.05f // 0.85f

        // Has transaction ID / RRN / Remarks
        if (transactionId != null) confidence += 0.1f // 0.95f

        // Reasonable amount
        if (amount in 100..10_000_000_00L) confidence += 0.05f // 1.0f

        return confidence.coerceIn(0f, 1f)
    }
}
