package com.moneybol.app.payments

import com.moneybol.app.core.model.PaymentEvent

/**
 * Duplicate detection for payment notifications.
 *
 * A single payment may generate multiple notifications:
 * - App notification
 * - Bank notification
 * - SMS
 * - Multiple notification updates
 *
 * MoneyBol must not announce the same transaction multiple times.
 *
 * Strategy (priority order):
 * 1. Transaction ID match: Same provider + same transactionId → duplicate
 * 2. Fingerprint match: Same provider + same amount + timestamp within window → potential duplicate
 *    (but only when no transaction ID exists to differentiate)
 *
 * IMPORTANT: Never deduplicate solely by amount.
 * Two different customers can pay Rs. 500 within seconds.
 */
class DuplicateDetector {

    // In-memory recent payment cache for fast dedup
    // (supplements database checks in PaymentRepositoryImpl)
    private val recentPayments = mutableListOf<RecentPayment>()
    private val maxCacheSize = 100
    private val windowMs = 60_000L // 60 seconds

    data class RecentPayment(
        val provider: String,
        val transactionId: String?,
        val amount: Long,
        val timestamp: Long,
        val contentHash: Int,
    )

    /**
     * Check if a payment event is a duplicate of a recently seen payment.
     *
     * @return true if this is a duplicate and should NOT be announced
     */
    @Synchronized
    fun isDuplicate(event: PaymentEvent): Boolean {
        cleanupOldEntries()

        // Priority 1: Transaction ID match
        if (event.transactionId != null) {
            val txIdMatch = recentPayments.any { recent ->
                recent.provider == event.provider &&
                    recent.transactionId == event.transactionId
            }
            if (txIdMatch) return true
        }

        // Priority 2: Fingerprint match (only when no txn ID)
        if (event.transactionId == null) {
            val fingerprintMatch = recentPayments.any { recent ->
                recent.provider == event.provider &&
                    recent.amount == event.amount &&
                    recent.transactionId == null &&
                    kotlin.math.abs(recent.timestamp - event.timestamp) <= windowMs
            }
            if (fingerprintMatch) return true
        }

        // Not a duplicate — add to cache
        recentPayments.add(
            RecentPayment(
                provider = event.provider,
                transactionId = event.transactionId,
                amount = event.amount,
                timestamp = event.timestamp,
                contentHash = event.hashCode(),
            )
        )

        // Trim cache
        if (recentPayments.size > maxCacheSize) {
            recentPayments.removeAt(0)
        }

        return false
    }

    /**
     * Remove entries older than the dedup window.
     */
    private fun cleanupOldEntries() {
        val cutoff = System.currentTimeMillis() - windowMs * 2
        recentPayments.removeAll { it.timestamp < cutoff }
    }

    /**
     * Clear the cache. Used for testing.
     */
    @Synchronized
    fun clear() {
        recentPayments.clear()
    }
}
