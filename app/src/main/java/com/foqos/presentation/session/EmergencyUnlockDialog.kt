package com.foqos.presentation.session

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Emergency unlock dialog with attempt tracking.
 */
@Composable
fun EmergencyUnlockDialog(
    attemptsRemaining: Int,
    maxAttempts: Int,
    cooldownEndTime: Long?,
    onUnlock: () -> Unit,
    onDismiss: () -> Unit
) {
    val attemptsUsed = maxAttempts - attemptsRemaining
    val isOnCooldown = cooldownEndTime != null && System.currentTimeMillis() < cooldownEndTime
    
    var cooldownRemaining by remember { mutableStateOf(0L) }
    
    // Update cooldown timer
    LaunchedEffect(cooldownEndTime) {
        if (cooldownEndTime != null) {
            while (System.currentTimeMillis() < cooldownEndTime) {
                cooldownRemaining = cooldownEndTime - System.currentTimeMillis()
                delay(1000)
            }
        }
    }
    
    // Pulse animation for warning
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "Emergency",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        title = {
            Text(
                "Emergency Unlock",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isOnCooldown) {
                    // Cooldown message
                    Text(
                        "You've used all emergency unlock attempts.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Try again in:",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        formatCooldown(cooldownRemaining),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // Warning message
                    Text(
                        "⚠️ This should only be used in genuine emergencies!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Each use weakens your commitment to focus. Use your physical unlock method instead.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Attempts remaining
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                attemptsRemaining <= 1 -> MaterialTheme.colorScheme.errorContainer
                                attemptsRemaining <= 2 -> Color(0xFFFFF3E0) // Orange
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Attempts Remaining",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                "$attemptsRemaining / $maxAttempts",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    attemptsRemaining <= 1 -> MaterialTheme.colorScheme.error
                                    attemptsRemaining <= 2 -> Color(0xFFFF9800) // Orange
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                    
                    if (attemptsUsed > 0) {
                        Text(
                            "You've already used $attemptsUsed emergency unlock${if (attemptsUsed > 1) "s" else ""} this session.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isOnCooldown) {
                Button(
                    onClick = onUnlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Use Emergency Unlock")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isOnCooldown) "Close" else "Cancel")
            }
        }
    )
}

private fun formatCooldown(milliseconds: Long): String {
    val minutes = (milliseconds / 60000).toInt()
    val seconds = ((milliseconds % 60000) / 1000).toInt()
    return String.format("%d:%02d", minutes, seconds)
}
