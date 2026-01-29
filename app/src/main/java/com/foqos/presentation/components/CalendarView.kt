package com.foqos.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDay(
    val date: Date,
    val dayOfMonth: Int,
    val dayOfWeek: String,
    val isToday: Boolean,
    val hasSession: Boolean
)

@Composable
fun CalendarView(
    completedSessionDates: List<Long>,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    val today = calendar.time

    // Generate 7 days centered on today (-3 to +3)
    val days = remember(completedSessionDates) {
        val daysList = mutableListOf<CalendarDay>()
        val tempCalendar = Calendar.getInstance()

        for (i in -3..3) {
            tempCalendar.time = today
            tempCalendar.add(Calendar.DAY_OF_MONTH, i)

            val date = tempCalendar.time
            val dayOfMonth = tempCalendar.get(Calendar.DAY_OF_MONTH)
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val dayOfWeek = dayFormat.format(date)

            // Check if this day has completed sessions
            val dayStart = tempCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val dayEnd = tempCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val hasSession = completedSessionDates.any { it in dayStart..dayEnd }

            daysList.add(
                CalendarDay(
                    date = date,
                    dayOfMonth = dayOfMonth,
                    dayOfWeek = dayOfWeek,
                    isToday = i == 0,
                    hasSession = hasSession
                )
            )
        }
        daysList
    }

    // Get current month/year
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthYear = monthYearFormat.format(today)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month/Year header
            Text(
                text = monthYear,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Horizontal scrolling calendar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(days) { day ->
                    CalendarDayItem(day = day)
                }
            }
        }
    }
}

@Composable
private fun CalendarDayItem(day: CalendarDay) {
    val containerColor = if (day.isToday) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (day.isToday) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .width(56.dp)
            .height(72.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Day of week
            Text(
                text = day.dayOfWeek,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            // Day of month
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            // Session indicator dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = if (day.hasSession) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            contentColor.copy(alpha = 0.0f)
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}
