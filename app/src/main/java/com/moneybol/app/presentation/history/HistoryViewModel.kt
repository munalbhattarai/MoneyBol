package com.moneybol.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybol.app.core.util.AmountParser
import com.moneybol.app.data.PaymentRepository
import com.moneybol.app.database.PaymentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class PaymentHistoryItem(
    val id: String,
    val provider: String,
    val amount: String,
    val time: String,
    val transactionId: String?,
)

data class GroupedPayments(
    val label: String,
    val payments: List<PaymentHistoryItem>,
    val total: String,
)

data class HistoryUiState(
    val groups: List<GroupedPayments> = emptyList(),
    val isEmpty: Boolean = true,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    init {
        observePayments()
    }

    private fun observePayments() {
        viewModelScope.launch {
            paymentRepository.getAllPayments().collect { payments ->
                val groups = groupPayments(payments)
                _uiState.value = HistoryUiState(
                    groups = groups,
                    isEmpty = payments.isEmpty(),
                )
            }
        }
    }

    private fun groupPayments(payments: List<PaymentEntity>): List<GroupedPayments> {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val now = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStart = calendar.timeInMillis

        calendar.timeInMillis = now
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.timeInMillis

        val groups = mutableListOf<GroupedPayments>()

        val today = payments.filter { it.timestamp >= todayStart }
        val yesterday = payments.filter { it.timestamp >= yesterdayStart && it.timestamp < todayStart }
        val thisWeek = payments.filter { it.timestamp >= weekStart && it.timestamp < yesterdayStart }
        val older = payments.filter { it.timestamp < weekStart }

        if (today.isNotEmpty()) {
            groups.add(createGroup("Today", today))
        }
        if (yesterday.isNotEmpty()) {
            groups.add(createGroup("Yesterday", yesterday))
        }
        if (thisWeek.isNotEmpty()) {
            groups.add(createGroup("This Week", thisWeek))
        }
        if (older.isNotEmpty()) {
            groups.add(createGroup("Earlier", older))
        }

        return groups
    }

    private fun createGroup(label: String, payments: List<PaymentEntity>): GroupedPayments {
        val total = payments.sumOf { it.amount }
        return GroupedPayments(
            label = label,
            payments = payments.map { entity ->
                PaymentHistoryItem(
                    id = entity.id,
                    provider = entity.providerDisplayName,
                    amount = "Rs. ${AmountParser.formatForDisplay(entity.amount)}",
                    time = timeFormat.format(Date(entity.timestamp)),
                    transactionId = entity.transactionId,
                )
            },
            total = "Rs. ${AmountParser.formatForDisplay(total)}",
        )
    }
}
