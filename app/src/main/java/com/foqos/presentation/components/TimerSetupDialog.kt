package com.foqos.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TimerSetupDialog(
    onDismiss: () -> Unit,
    onStartWithTimer: (durationMinutes: Int) -> Unit
) {
    var selectedDuration by remember { mutableIntStateOf(30) }
    val durations = listOf(15, 30, 60, 120, 240) // 15min, 30min, 1hr, 2hr, 4hr

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Filled.Timer,
                contentDescription = "Timer",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Set Focus Duration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Select how long you want to focus. The session will automatically end after this duration, or you can unlock early with your NFC tag.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "Duration",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durations.forEach { duration ->
                        DurationOption(
                            duration = duration,
                            isSelected = selectedDuration == duration,
                            onSelect = { selectedDuration = duration }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStartWithTimer(selectedDuration) }
            ) {
                Text("Start Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DurationOption(
    duration: Int,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun formatDuration(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes minutes"
        minutes == 60 -> "1 hour"
        minutes % 60 == 0 -> "${minutes / 60} hours"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}
