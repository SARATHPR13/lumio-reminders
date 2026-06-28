package com.lumio.app.presentation.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val color: Color,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            emoji       = "🔔",
            title       = "Welcome to LUMIO",
            description = "Your smart reminder app. Never miss what matters — birthdays, bills, medicine, meetings and more.",
            color       = Color(0xFF1A73E8),
            icon        = Icons.Rounded.NotificationsActive
        ),
        OnboardingPage(
            emoji       = "🎙️",
            title       = "Voice Reminders",
            description = "Just say \"Remind me to call mom tomorrow at 5 PM\" and LUMIO understands you instantly.",
            color       = Color(0xFF7B2FBE),
            icon        = Icons.Rounded.Mic
        ),
        OnboardingPage(
            emoji       = "📂",
            title       = "Smart Categories",
            description = "Organize reminders into Work, Health, Shopping, Bills and more. Find anything instantly.",
            color       = Color(0xFF00897B),
            icon        = Icons.Rounded.Category
        ),
        OnboardingPage(
            emoji       = "🔒",
            title       = "Private & Secure",
            description = "PIN lock and fingerprint protection keep your reminders private. Your data stays on your device.",
            color       = Color(0xFFFF6B35),
            icon        = Icons.Rounded.Security
        ),
        OnboardingPage(
            emoji       = "🚀",
            title       = "You're All Set!",
            description = "Start adding reminders and let LUMIO help you stay organized every day.",
            color       = Color(0xFF2E7D32),
            icon        = Icons.Rounded.CheckCircle
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingPage(page = pages[pageIndex])
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { index ->
                    val isSelected  = pagerState.currentPage == index
                    val color       = pages[pagerState.currentPage].color
                    val width by animateDpAsState(
                        targetValue   = if (isSelected) 24.dp else 8.dp,
                        animationSpec = tween(300),
                        label         = "dot"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) color
                                else color.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Buttons
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    // Skip
                    OutlinedButton(
                        onClick  = onFinish,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Text("Skip", fontWeight = FontWeight.Medium)
                    }
                    // Next
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier.weight(2f).height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = pages[pagerState.currentPage].color
                        )
                    ) {
                        Text("Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Rounded.ArrowForward, null, modifier = Modifier.size(18.dp))
                    }
                } else {
                    // Get Started
                    Button(
                        onClick  = onFinish,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = pages.last().color
                        )
                    ) {
                        Icon(Icons.Rounded.RocketLaunch, null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Get Started!", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    val infiniteAnim = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteAnim.animateFloat(
        initialValue  = 0f,
        targetValue   = -16f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(y = offsetY.dp)
                .clip(CircleShape)
                .background(page.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(page.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(page.emoji, fontSize = 56.sp)
            }
        }

        Spacer(Modifier.height(48.dp))

        Text(
            text       = page.title,
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text      = page.description,
            style     = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 26.sp
        )

        Spacer(Modifier.height(120.dp))
    }
}
