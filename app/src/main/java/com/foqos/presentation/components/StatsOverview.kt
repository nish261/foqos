package com.foqos.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foqos.util.TimeFormatter

data class TodayStats(
    val blockedAppsCount: Int,
    val blockedDomainsCount: Int,
    val totalSessions: Int,
    val focusTimeMills: Long
)

@Composable
fun StatsOverview(
    stats: TodayStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Today's Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Apps stat
                StatItem(
                    icon = {
                        Icon(
                            Icons.Filled.Phone,
                            contentDescription = "Apps",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    label = "Apps & Categories",
                    value = stats.blockedAppsCount.toString(),
                    modifier = Modifier.weight(1f)
                )

                // Domains stat
                StatItem(
                    icon = {
                        Icon(
                            Icons.Filled.Language,
                            contentDescription = "Domains",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    label = "Domains",
                    value = stats.blockedDomainsCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sessions stat
                StatItem(
                    icon = {
                        Text(
                            text = "⏱️",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    label = "Total Sessions",
                    value = stats.totalSessions.toString(),
                    modifier = Modifier.weight(1f)
                )

                // Focus time stat
                StatItem(
                    icon = {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = "Focus Time",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    label = "Focus Time",
                    value = formatFocusTime(stats.focusTimeMills),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun formatFocusTime(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "0m"
    }
}
