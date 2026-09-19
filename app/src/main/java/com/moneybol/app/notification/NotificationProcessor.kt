package com.moneybol.app.notification

import com.moneybol.app.core.model.AnnouncementStatus
import com.moneybol.app.core.model.PaymentDirection
import com.moneybol.app.core.model.RawNotification
import com.moneybol.app.audio.AnnouncementManager
import com.moneybol.app.data.PaymentRepository
import com.moneybol.app.database.PaymentEntity
import com.moneybol.app.providers.ProviderResolver
import com.moneybol.app.settings.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Notification processing pipeline.
 *
 * RawNotification → ProviderResolver → Parser → PaymentEvent
 *   → Validation → Duplicate Check → Save → Announce
 *
 * All processing runs on Dispatchers.Default (off main thread).
 */
@Singleton
class NotificationProcessor @Inject constructor(
    private val providerResolver: ProviderResolver,
    private val paymentRepository: PaymentRepository,
    private val announcementManager: AnnouncementManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Process a raw notification through the full pipeline.
     * Called from NotificationListenerService — must not block.
     */
    fun process(rawNotification: RawNotification) {
        scope.launch {
            processInternal(rawNotification)
        }
    }

    private suspend fun processInternal(rawNotification: RawNotification) {
        // Step 1: Check if listening is enabled
        val isEnabled = userPreferencesRepository.listeningEnabled.first()
        if (!isEnabled) return

        // Step 2: Find a parser that can handle this notification
        val parser = providerResolver.resolve(rawNotification) ?: return

        // Step 3: Parse the notification into a PaymentEvent
        val paymentEvent = parser.parse(rawNotification) ?: return

        // Step 4: Validate — only incoming credits with sufficient confidence
        if (paymentEvent.direction != PaymentDirection.CREDIT) return
        if (paymentEvent.confidence < 0.7f) return
        if (paymentEvent.amount <= 0) return

        // Step 5: Check for duplicates
        val isDuplicate = paymentRepository.isDuplicate(
            provider = paymentEvent.provider,
            transactionId = paymentEvent.transactionId,
            amount = paymentEvent.amount,
            timestamp = paymentEvent.timestamp,
        )

        // Step 6: Save to database
        val entity = PaymentEntity(
            id = paymentEvent.id,
            provider = paymentEvent.provider,
            providerDisplayName = paymentEvent.providerDisplayName,
            amount = paymentEvent.amount,
            currency = paymentEvent.currency,
            transactionId = paymentEvent.transactionId,
            payerName = paymentEvent.payerName,
            timestamp = paymentEvent.timestamp,
            announcementStatus = if (isDuplicate) {
                AnnouncementStatus.SKIPPED.name
            } else {
                AnnouncementStatus.ANNOUNCED.name
            },
            confidence = paymentEvent.confidence,
        )
        paymentRepository.savePayment(entity)

        // Step 7: Announce (if not duplicate)
        if (!isDuplicate) {
            announcementManager.announce(paymentEvent)
        }
    }
}
