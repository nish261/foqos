package com.foqos.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foqos.data.local.entity.BlockedProfileEntity
import com.foqos.domain.model.BlockingStrategy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCard(
    profile: BlockedProfileEntity,
    isActive: Boolean,
    onStart: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strategy = BlockingStrategy.fromId(profile.blockingStrategyId)

    // Animate colors
    val containerColor by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "cardColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (isActive) 4.dp else 2.dp,
        animationSpec = tween(300),
        label = "cardElevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strategy.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isActive) {
                        IconButton(onClick = onStart) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Start Session",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete Profile",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // App and domain count
            if (profile.selectedApps.isNotEmpty() || !profile.domains.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val parts = mutableListOf<String>()
                if (profile.selectedApps.isNotEmpty()) {
                    parts.add("${profile.selectedApps.size} apps")
                }
                if (!profile.domains.isNullOrEmpty()) {
                    parts.add("${profile.domains.size} websites")
                }
                Text(
                    text = parts.joinToString(" • ") + " blocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }
            
            // Features
            if (profile.enableBreaks || (profile.reminderTimeInSeconds != null) || profile.enableStrictMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (profile.enableBreaks) {
                        FeatureChip("Breaks")
                    }
                    if (profile.reminderTimeInSeconds != null) {
                        FeatureChip("Reminders")
                    }
                    if (profile.enableStrictMode) {
                        FeatureChip("Strict Mode")
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
