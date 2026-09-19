package com.moneybol.app.data

import com.moneybol.app.database.PaymentDao
import com.moneybol.app.database.PaymentEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of PaymentRepository.
 */
@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao
) : PaymentRepository {

    override suspend fun savePayment(payment: PaymentEntity) {
        paymentDao.insert(payment)
    }

    override fun getLastPayment(): Flow<PaymentEntity?> {
        return paymentDao.getLastPayment()
    }

    override fun getTodayTotal(): Flow<Long> {
        return paymentDao.getTodayTotal(getStartOfToday())
    }

    override fun getTodayCount(): Flow<Int> {
        return paymentDao.getTodayCount(getStartOfToday())
    }

    override fun getPaymentsByDateRange(start: Long, end: Long): Flow<List<PaymentEntity>> {
        return paymentDao.getPaymentsByDateRange(start, end)
    }

    override fun getAllPayments(): Flow<List<PaymentEntity>> {
        return paymentDao.getAllPayments()
    }

    override suspend fun isDuplicate(
        provider: String,
        transactionId: String?,
        amount: Long,
        timestamp: Long
    ): Boolean {
        // Priority 1: Transaction ID match
        if (transactionId != null) {
            val count = paymentDao.countByProviderAndTransactionId(provider, transactionId)
            if (count > 0) return true
        }

        // Priority 2: Provider + amount + timestamp window (60 seconds)
        // This catches duplicate notifications from same provider for same payment
        val windowMs = 60_000L
        val count = paymentDao.countByProviderAmountWindow(
            provider = provider,
            amount = amount,
            windowStart = timestamp - windowMs,
            windowEnd = timestamp + windowMs,
        )
        // Only consider duplicate if same provider, same amount, within window
        // AND we don't have a transaction ID to differentiate
        if (transactionId == null && count > 0) return true

        return false
    }

    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
