package com.moneybol.app.presentation.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneybol.app.R

/**
 * Test announcement button with loading state.
 */
@Composable
fun TestAnnouncementButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 0.97f else 1f,
        animationSpec = tween(150),
        label = "test_scale"
    )

    Button(
        onClick = onClick,
        enabled = !isPlaying,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    ) {
        if (isPlaying) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Playing…",
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Campaign,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.test_announcement),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Repeat last payment button.
 */
@Composable
fun RepeatLastPaymentButton(
    isPlaying: Boolean,
    lastPaymentAmount: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isPlaying && lastPaymentAmount != null,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isPlaying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Repeating…")
            } else {
                Icon(
                    imageVector = Icons.Rounded.Replay,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lastPaymentAmount != null) {
                        "${stringResource(R.string.repeat_last)} · $lastPaymentAmount"
                    } else {
                        stringResource(R.string.repeat_last)
                    }
                )
            }
        }
    }
}
