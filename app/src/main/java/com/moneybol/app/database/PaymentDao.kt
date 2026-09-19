package com.moneybol.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for payment records.
 * Uses reactive Flow queries — no expensive operations during recomposition.
 */
@Dao
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity)

    @Query("SELECT * FROM payments ORDER BY timestamp DESC LIMIT 1")
    fun getLastPayment(): Flow<PaymentEntity?>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM payments
        WHERE timestamp >= :startOfDay
        AND announcementStatus = 'ANNOUNCED'
    """)
    fun getTodayTotal(startOfDay: Long): Flow<Long>

    @Query("""
        SELECT COUNT(*) FROM payments
        WHERE timestamp >= :startOfDay
        AND announcementStatus = 'ANNOUNCED'
    """)
    fun getTodayCount(startOfDay: Long): Flow<Int>

    @Query("""
        SELECT * FROM payments
        WHERE timestamp >= :start AND timestamp < :end
        ORDER BY timestamp DESC
    """)
    fun getPaymentsByDateRange(start: Long, end: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("""
        SELECT COUNT(*) FROM payments
        WHERE provider = :provider
        AND transactionId = :transactionId
        AND transactionId IS NOT NULL
    """)
    suspend fun countByProviderAndTransactionId(provider: String, transactionId: String): Int

    @Query("""
        SELECT COUNT(*) FROM payments
        WHERE provider = :provider
        AND amount = :amount
        AND timestamp >= :windowStart
        AND timestamp <= :windowEnd
    """)
    suspend fun countByProviderAmountWindow(
        provider: String,
        amount: Long,
        windowStart: Long,
        windowEnd: Long
    ): Int

    @Query("DELETE FROM payments WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
