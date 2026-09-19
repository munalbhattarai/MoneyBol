package com.moneybol.app.core.model

/**
 * Information about a payment notification provider (wallet, bank, payment network).
 */
data class ProviderInfo(
    val id: String,
    val displayName: String,
    val packageNames: List<String>,
    val status: ProviderStatus,
    val category: ProviderCategory,
    val enabled: Boolean = true,
    val iconResName: String? = null
)

/**
 * Category of payment provider.
 */
enum class ProviderCategory {
    WALLET,
    BANK,
    PAYMENT_NETWORK,
    OTHER
}
