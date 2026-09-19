package com.moneybol.app.core.model

/**
 * Verification status of a payment notification provider.
 *
 * A provider is only VERIFIED after real sanitized notification
 * samples have passed automated and physical-device tests.
 */
enum class ProviderStatus {
    /** Real notification samples tested and passing on physical device. */
    VERIFIED,

    /** Some notification formats tested, others unknown. */
    PARTIALLY_VERIFIED,

    /** Adapter exists but no verified real notification samples. */
    FORMAT_UNVERIFIED,

    /** Provider is not supported. */
    UNSUPPORTED
}
