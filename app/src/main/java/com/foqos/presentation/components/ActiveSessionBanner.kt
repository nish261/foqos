package com.foqos.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foqos.data.local.entity.BlockedProfileEntity
import com.foqos.data.local.entity.BlockedProfileSessionEntity
import com.foqos.util.TimeFormatter
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ActiveSessionBanner(
    profile: BlockedProfileEntity,
    session: BlockedProfileSessionEntity,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    remoteLockActive: Boolean = false
) {
    // Update elapsed time every second
    var elapsedTime by remember { mutableStateOf(session.getTotalDuration()) }

    LaunchedEffect(session.id, session.breakStartTime) {
        while (true) {
            elapsedTime = session.getTotalDuration()
            delay(1.seconds)
        }
    }
    
    // Pulse animation for active state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Animate container color based on pause state
    val containerColor by animateColorAsState(
        targetValue = if (session.breakStartTime != null) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = tween(300),
        label = "containerColor"
    )

    val onContainerColor = if (session.breakStartTime != null) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp),
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Animated status text
                AnimatedContent(
                    targetState = session.breakStartTime != null,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "statusText"
                ) { isPaused ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isPaused) "⏸️ Session Paused" else "🎯 Session Active",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = onContainerColor
                        )
                        if (!isPaused) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                                        shape = MaterialTheme.shapes.extraSmall
                                    )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainerColor.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = TimeFormatter.formatElapsedTime(elapsedTime),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (session.breakStartTime != null) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
            
            if (remoteLockActive) {
                FilledTonalButton(
                    onClick = { /* Do nothing - remote lock active */ },
                    enabled = false,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.Filled.Stop,
                        contentDescription = "Stop Disabled",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Locked")
                }
            } else {
                FilledTonalButton(
                    onClick = onStop,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        Icons.Filled.Stop,
                        contentDescription = "Stop Session",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop")
                }
            }
        }
    }
}
