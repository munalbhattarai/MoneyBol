package com.moneybol.app.data

import com.moneybol.app.database.PaymentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for payment data.
 * Abstraction over Room for testability.
 */
interface PaymentRepository {
    suspend fun savePayment(payment: PaymentEntity)
    fun getLastPayment(): Flow<PaymentEntity?>
    fun getTodayTotal(): Flow<Long>
    fun getTodayCount(): Flow<Int>
    fun getPaymentsByDateRange(start: Long, end: Long): Flow<List<PaymentEntity>>
    fun getAllPayments(): Flow<List<PaymentEntity>>
    suspend fun isDuplicate(provider: String, transactionId: String?, amount: Long, timestamp: Long): Boolean
}
