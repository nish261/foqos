package com.foqos.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar

data class ScheduleConfig(
    val daysOfWeek: List<Int>, // 1=Monday, 7=Sunday
    val startTime: String, // HH:mm format
    val endTime: String // HH:mm format
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSetupDialog(
    currentSchedule: ScheduleConfig?,
    onDismiss: () -> Unit,
    onSave: (ScheduleConfig) -> Unit
) {
    var selectedDays by remember { mutableStateOf(currentSchedule?.daysOfWeek?.toSet() ?: emptySet()) }
    var startHour by remember { mutableStateOf(currentSchedule?.startTime?.split(":")?.get(0)?.toIntOrNull() ?: 9) }
    var startMinute by remember { mutableStateOf(currentSchedule?.startTime?.split(":")?.get(1)?.toIntOrNull() ?: 0) }
    var endHour by remember { mutableStateOf(currentSchedule?.endTime?.split(":")?.get(0)?.toIntOrNull() ?: 17) }
    var endMinute by remember { mutableStateOf(currentSchedule?.endTime?.split(":")?.get(1)?.toIntOrNull() ?: 0) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Schedule") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Day selector
                Text(
                    "Active Days",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val days = listOf(
                    1 to "Mon",
                    2 to "Tue",
                    3 to "Wed",
                    4 to "Thu",
                    5 to "Fri",
                    6 to "Sat",
                    7 to "Sun"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.forEach { (dayNum, dayLabel) ->
                        FilterChip(
                            selected = selectedDays.contains(dayNum),
                            onClick = {
                                selectedDays = if (selectedDays.contains(dayNum)) {
                                    selectedDays - dayNum
                                } else {
                                    selectedDays + dayNum
                                }
                            },
                            label = { Text(dayLabel) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Divider()

                // Start time
                Text(
                    "Start Time",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showStartTimePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.AccessTime, "Start Time")
                            Text("Start Time")
                        }
                        Text(
                            String.format("%02d:%02d", startHour, startMinute),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // End time
                Text(
                    "End Time",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showEndTimePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.AccessTime, "End Time")
                            Text("End Time")
                        }
                        Text(
                            String.format("%02d:%02d", endHour, endMinute),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedDays.isNotEmpty()) {
                        onSave(
                            ScheduleConfig(
                                daysOfWeek = selectedDays.sorted(),
                                startTime = String.format("%02d:%02d", startHour, startMinute),
                                endTime = String.format("%02d:%02d", endHour, endMinute)
                            )
                        )
                    }
                },
                enabled = selectedDays.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = startHour,
            initialMinute = startMinute,
            is24Hour = true
        )

        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                startHour = timePickerState.hour
                startMinute = timePickerState.minute
                showStartTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = endHour,
            initialMinute = endMinute,
            is24Hour = true
        )

        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                endHour = timePickerState.hour
                endMinute = timePickerState.minute
                showEndTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
