package com.moneybol.app.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneybol.app.R
import com.moneybol.app.presentation.theme.ListeningGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pageCount = 6
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    icon = Icons.Rounded.VolumeUp,
                    title = stringResource(R.string.onboarding_welcome_title),
                    description = stringResource(R.string.onboarding_welcome_subtitle),
                    isWelcome = true,
                )
                1 -> OnboardingPage(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.onboarding_how_title),
                    description = stringResource(R.string.onboarding_how_desc),
                )
                2 -> OnboardingPage(
                    icon = Icons.Rounded.Lock,
                    title = stringResource(R.string.onboarding_privacy_title),
                    description = stringResource(R.string.onboarding_privacy_desc),
                )
                3 -> OnboardingPage(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.onboarding_permission_title),
                    description = stringResource(R.string.onboarding_permission_desc),
                )
                4 -> OnboardingPage(
                    icon = Icons.Rounded.Payments,
                    title = stringResource(R.string.onboarding_providers_title),
                    description = stringResource(R.string.onboarding_providers_desc),
                )
                5 -> OnboardingPage(
                    icon = Icons.Rounded.Campaign,
                    title = stringResource(R.string.onboarding_test_title),
                    description = "Tap the button below to hear a test announcement.",
                    showTestButton = true,
                    onTestAnnouncement = { viewModel.playTestAnnouncement() },
                )
            }
        }

        // Page indicators + navigation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Dot indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 24.dp),
            ) {
                repeat(pageCount) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                    )
                }
            }

            // Navigation buttons
            if (pagerState.currentPage == pageCount - 1) {
                Button(
                    onClick = {
                        viewModel.completeOnboarding()
                        onOnboardingComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.get_started),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        onClick = {
                            viewModel.completeOnboarding()
                            onOnboardingComplete()
                        }
                    ) {
                        Text(stringResource(R.string.skip))
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(stringResource(R.string.next))
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    icon: ImageVector,
    title: String,
    description: String,
    isWelcome: Boolean = false,
    showTestButton: Boolean = false,
    onTestAnnouncement: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(if (isWelcome) 80.dp else 64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = if (isWelcome) {
                MaterialTheme.typography.displaySmall
            } else {
                MaterialTheme.typography.headlineMedium
            },
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp,
        )

        if (showTestButton && onTestAnnouncement != null) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onTestAnnouncement,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ListeningGreen,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Campaign,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test announcement")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = ListeningGreen,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_ready),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ListeningGreen,
                )
            }
        }
    }
}
