package com.moneybol.app.providers

import com.moneybol.app.core.model.PaymentEvent
import com.moneybol.app.core.model.RawNotification

/**
 * Interface for payment notification parsers.
 *
 * Each provider (eSewa, Khalti, banks, etc.) implements this interface
 * with its own parsing logic.
 *
 * IMPORTANT: Do NOT use AI/LLM for payment classification.
 * Use deterministic code only.
 */
interface PaymentNotificationParser {

    /**
     * Unique identifier for this provider.
     */
    val providerId: String

    /**
     * Human-readable display name.
     */
    val providerDisplayName: String

    /**
     * Package names this parser handles.
     */
    val supportedPackageNames: Set<String>

    /**
     * Check if this parser can handle the given notification.
     *
     * @return true if this parser should attempt to parse the notification
     */
    fun canHandle(notification: RawNotification): Boolean

    /**
     * Parse a notification into a PaymentEvent.
     *
     * @return PaymentEvent if successfully parsed, null if parsing fails
     *         or the notification is not a qualifying payment
     */
    fun parse(notification: RawNotification): PaymentEvent?
}
