package com.moneybol.app.providers

import com.moneybol.app.core.model.RawNotification
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves which parser should handle a given notification.
 *
 * Resolution priority:
 * 1. Provider-specific parsers (eSewa, Khalti, etc.) by package name
 * 2. Generic bank parser as fallback
 */
@Singleton
class ProviderResolver @Inject constructor(
    private val parsers: Set<@JvmSuppressWildcards PaymentNotificationParser>
) {

    /**
     * Find the appropriate parser for a notification.
     *
     * @return The parser that can handle this notification, or null if none match
     */
    fun resolve(notification: RawNotification): PaymentNotificationParser? {
        // First try specific parsers by package name
        for (parser in parsers) {
            if (notification.packageName in parser.supportedPackageNames) {
                if (parser.canHandle(notification)) {
                    return parser
                }
            }
        }

        // Then try any parser that claims it can handle (for generic parsers)
        for (parser in parsers) {
            if (notification.packageName !in parser.supportedPackageNames) {
                if (parser.canHandle(notification)) {
                    return parser
                }
            }
        }

        return null
    }
}
