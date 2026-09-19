package com.moneybol.app.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneybol.app.R
import com.moneybol.app.core.model.ListeningState
import com.moneybol.app.presentation.theme.ErrorRed
import com.moneybol.app.presentation.theme.ListeningGreen
import com.moneybol.app.presentation.theme.PausedAmber

/**
 * Listening status card with animated indicator dot.
 */
@Composable
fun ListeningStatusCard(
    listeningState: ListeningState,
    onEnablePermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val indicatorColor by animateColorAsState(
        targetValue = when (listeningState) {
            ListeningState.LISTENING -> ListeningGreen
            ListeningState.PAUSED -> PausedAmber
            ListeningState.PERMISSION_REQUIRED -> ErrorRed
            ListeningState.SERVICE_PROBLEM -> ErrorRed
        },
        animationSpec = tween(durationMillis = 300),
        label = "indicator_color"
    )

    // Pulsing animation for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (listeningState == ListeningState.LISTENING) 1.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Animated pulsing dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .scale(pulseScale)
                        .background(
                            color = indicatorColor,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when (listeningState) {
                        ListeningState.LISTENING -> stringResource(R.string.status_listening)
                        ListeningState.PAUSED -> stringResource(R.string.status_paused)
                        ListeningState.PERMISSION_REQUIRED -> stringResource(R.string.status_permission_required)
                        ListeningState.SERVICE_PROBLEM -> stringResource(R.string.status_service_problem)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = indicatorColor,
                )
            }

            Text(
                text = when (listeningState) {
                    ListeningState.LISTENING -> stringResource(R.string.status_ready)
                    ListeningState.PAUSED -> "Announcements paused"
                    ListeningState.PERMISSION_REQUIRED -> "Tap to enable notification access"
                    ListeningState.SERVICE_PROBLEM -> "Tap to troubleshoot"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (listeningState == ListeningState.PERMISSION_REQUIRED) {
                TextButton(
                    onClick = onEnablePermission,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.enable_notification_access),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
