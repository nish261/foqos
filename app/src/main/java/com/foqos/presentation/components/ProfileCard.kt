package com.foqos.presentation.components

import androidx.compose.animation.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foqos.data.local.entity.BlockedProfileEntity
import com.foqos.domain.model.BlockingStrategy
import com.foqos.util.GradientPresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCard(
    profile: BlockedProfileEntity,
    isActive: Boolean,
    sessionCount: Int = 0,
    onStart: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strategy = BlockingStrategy.fromId(profile.blockingStrategyId)
    val gradient = GradientPresets.getGradient(profile.gradientId)

    val elevation by animateDpAsState(
        targetValue = if (isActive) 6.dp else 3.dp,
        animationSpec = tween(300),
        label = "cardElevation"
    )

    // Options menu state
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isActive) {
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            )
                        )
                    } else {
                        gradient
                    }
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header row with name and menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strategy.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    if (!isActive) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete Profile") },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatBadge(
                        icon = "📱",
                        value = "${profile.selectedApps.size}",
                        label = "apps"
                    )
                    StatBadge(
                        icon = "🌐",
                        value = "${profile.domains?.size ?: 0}",
                        label = "sites"
                    )
                    StatBadge(
                        icon = "⏱️",
                        value = "$sessionCount",
                        label = "sessions"
                    )
                }

                // Allow Mode Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (profile.appsAllowMode) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "📱 Apps: Allow Mode",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (profile.domainsAllowMode) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "🌐 Domains: Allow Mode",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Schedule indicator
                if (profile.scheduleEnabled && profile.scheduleStartTime != null && profile.scheduleEndTime != null) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Schedule: ${formatSchedule(profile)}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "No Schedule Set",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Hold to Start button (only when not active)
                if (!isActive) {
                    HoldToStartButton(
                        onStart = onStart,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Active indicator
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "⏳ Session Active",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(
    icon: String,
    value: String,
    label: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

private fun formatSchedule(profile: BlockedProfileEntity): String {
    val days = profile.scheduleDaysOfWeek
    val start = profile.scheduleStartTime ?: "00:00"
    val end = profile.scheduleEndTime ?: "23:59"

    val dayStr = when {
        days == null || days.isEmpty() -> "Every day"
        days.size == 7 -> "Every day"
        days.size == 5 && days.containsAll(listOf(1, 2, 3, 4, 5)) -> "Mon-Fri"
        days.size == 2 && days.containsAll(listOf(6, 7)) -> "Weekends"
        else -> "${days.size} days"
    }

    return "$dayStr $start-$end"
}
