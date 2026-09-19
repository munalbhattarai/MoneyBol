package com.moneybol.app.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for payment records.
 *
 * Amount is stored in paisa (1 rupee = 100 paisa).
 * No full raw notification text is stored for privacy.
 */
@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["provider", "transactionId"]),
    ]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String,
    val provider: String,
    val providerDisplayName: String,
    val amount: Long,
    val currency: String = "NPR",
    val transactionId: String? = null,
    val payerName: String? = null,
    val timestamp: Long,
    val announcementStatus: String,
    val confidence: Float,
)
