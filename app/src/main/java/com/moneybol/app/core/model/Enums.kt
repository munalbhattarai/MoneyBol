package com.moneybol.app.core.model

/**
 * Announcement format preferences for voice announcements.
 */
enum class AnnouncementFormat(val templateKey: String, val example: String) {
    /** "Payment received. Five hundred rupees." */
    PAYMENT_RECEIVED_AMOUNT(
        templateKey = "payment_received_amount",
        example = "Payment received. Five hundred rupees."
    ),

    /** "Five hundred rupees received." */
    AMOUNT_RECEIVED(
        templateKey = "amount_received",
        example = "Five hundred rupees received."
    ),

    /** "Payment received, Rs. 500." */
    PAYMENT_RECEIVED_RS(
        templateKey = "payment_received_rs",
        example = "Payment received, Rs. 500."
    )
}

/**
 * Status of a payment announcement.
 */
enum class AnnouncementStatus {
    /** Successfully announced via TTS. */
    ANNOUNCED,

    /** Announcement failed (TTS error, etc.). */
    FAILED,

    /** Skipped (duplicate, low confidence, user paused, etc.). */
    SKIPPED
}

/**
 * Listening state of the MoneyBol service.
 */
enum class ListeningState {
    /** Actively listening for notifications. */
    LISTENING,

    /** User has paused listening. */
    PAUSED,

    /** Notification access permission not granted. */
    PERMISSION_REQUIRED,

    /** Service encountered a problem. */
    SERVICE_PROBLEM
}
