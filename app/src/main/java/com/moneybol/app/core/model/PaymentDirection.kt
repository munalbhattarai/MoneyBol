package com.moneybol.app.core.model

/**
 * Direction of a payment transaction.
 *
 * Safety rule: If direction cannot be confidently established → UNKNOWN.
 * UNKNOWN transactions are NOT announced.
 */
enum class PaymentDirection {
    /** Incoming payment (money received). Announce. */
    CREDIT,

    /** Outgoing payment (money sent). Do NOT announce. */
    DEBIT,

    /** Failed transaction. Do NOT announce. */
    FAILED,

    /** Pending transaction. Do NOT announce. */
    PENDING,

    /** OTP or verification code. Do NOT announce. NEVER speak OTPs. */
    OTP,

    /** Direction could not be determined. Do NOT announce. */
    UNKNOWN
}
