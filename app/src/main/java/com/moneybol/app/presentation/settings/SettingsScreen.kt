package com.moneybol.app.presentation.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneybol.app.R
import com.moneybol.app.core.model.AnnouncementFormat
import com.moneybol.app.core.model.ProviderStatus
import com.moneybol.app.presentation.theme.ListeningGreen
import com.moneybol.app.presentation.theme.PausedAmber
import com.moneybol.app.providers.ProviderRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Voice Settings ──
            SettingsSection(title = stringResource(R.string.voice_settings), icon = Icons.AutoMirrored.Rounded.VolumeUp) {
                // Announcement Format
                Text(
                    text = "Announcement format",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                AnnouncementFormat.entries.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = uiState.announcementFormat == format,
                            onClick = { viewModel.setAnnouncementFormat(format) },
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = format.example,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Speech Rate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Speech rate: ${String.format("%.1fx", uiState.speechRate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Slider(
                    value = uiState.speechRate,
                    onValueChange = { viewModel.setSpeechRate(it) },
                    valueRange = 0.5f..2.0f,
                    steps = 5,
                )
            }

            // ── Payment Sources ──
            SettingsSection(title = stringResource(R.string.payment_sources), icon = Icons.Rounded.Payments) {
                ProviderRegistry.providers.forEach { provider ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = provider.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (statusIcon, statusColor, statusText) = when (provider.status) {
                                    ProviderStatus.VERIFIED -> Triple(
                                        Icons.Rounded.CheckCircle,
                                        ListeningGreen,
                                        stringResource(R.string.provider_verified)
                                    )
                                    ProviderStatus.PARTIALLY_VERIFIED -> Triple(
                                        Icons.Rounded.Warning,
                                        PausedAmber,
                                        stringResource(R.string.provider_partially_verified)
                                    )
                                    ProviderStatus.FORMAT_UNVERIFIED -> Triple(
                                        Icons.Rounded.Warning,
                                        PausedAmber,
                                        stringResource(R.string.provider_unverified)
                                    )
                                    ProviderStatus.UNSUPPORTED -> Triple(
                                        Icons.Rounded.Info,
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                        stringResource(R.string.provider_unsupported)
                                    )
                                }
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = " $statusText",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = statusColor,
                                )
                            }
                        }

                        Switch(
                            checked = provider.enabled,
                            onCheckedChange = { /* Will be wired to preferences */ },
                        )
                    }
                }
            }

            // ── Notification Settings ──
            SettingsSection(title = stringResource(R.string.notification_settings), icon = Icons.Rounded.Notifications) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Notification Access",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = if (uiState.hasNotificationAccess) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.hasNotificationAccess) ListeningGreen
                                else MaterialTheme.colorScheme.error,
                        )
                    }
                    if (!uiState.hasNotificationAccess) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Enable")
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Listening",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Switch(
                        checked = uiState.listeningEnabled,
                        onCheckedChange = { viewModel.setListeningEnabled(it) },
                    )
                }
            }

            // ── Privacy ──
            SettingsSection(title = stringResource(R.string.privacy), icon = Icons.Rounded.Security) {
                Text(
                    text = stringResource(R.string.permission_notification_access_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── About ──
            SettingsSection(title = stringResource(R.string.about), icon = Icons.Rounded.Info) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "v1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
