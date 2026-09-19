package com.moneybol.app.presentation.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneybol.app.R
import com.moneybol.app.presentation.home.components.LastPaymentCard
import com.moneybol.app.presentation.home.components.ListeningStatusCard
import com.moneybol.app.presentation.home.components.RepeatLastPaymentButton
import com.moneybol.app.presentation.home.components.TestAnnouncementButton
import com.moneybol.app.presentation.home.components.TodaySummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Refresh listening state when screen resumes (e.g., after enabling permission)
    LaunchedEffect(Unit) {
        viewModel.refreshListeningState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = stringResource(R.string.history),
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Listening status
            ListeningStatusCard(
                listeningState = uiState.listeningState,
                onEnablePermission = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                },
            )

            // Last payment
            LastPaymentCard(
                provider = uiState.lastPaymentProvider,
                amount = uiState.lastPaymentAmount,
                time = uiState.lastPaymentTime,
            )

            // Today's summary
            TodaySummaryCard(
                totalAmount = uiState.todayTotal,
                paymentCount = uiState.todayCount,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Test announcement
            TestAnnouncementButton(
                isPlaying = uiState.isTestPlaying,
                onClick = { viewModel.onTestAnnouncement() },
            )

            // Repeat last payment
            AnimatedVisibility(
                visible = uiState.lastPaymentAmount != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                RepeatLastPaymentButton(
                    isPlaying = uiState.isRepeatPlaying,
                    lastPaymentAmount = uiState.lastPaymentAmount,
                    onClick = { viewModel.onRepeatLastPayment() },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
