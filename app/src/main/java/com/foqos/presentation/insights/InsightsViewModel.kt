package com.foqos.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foqos.data.local.entity.BlockedProfileSessionEntity
import com.foqos.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    val recentSessions: StateFlow<List<BlockedProfileSessionEntity>> = sessionRepository
        .getRecentSessions(limit = 100)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val statistics: StateFlow<SessionStatistics> = recentSessions
        .map { sessions ->
            calculateStatistics(sessions)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionStatistics()
        )
    
    private fun calculateStatistics(sessions: List<BlockedProfileSessionEntity>): SessionStatistics {
        val completedSessions = sessions.filter { it.endTime != null }
        
        if (completedSessions.isEmpty()) {
            return SessionStatistics()
        }
        
        val totalDuration = completedSessions.sumOf { it.getTotalDuration() }
        val averageDuration = totalDuration / completedSessions.size
        val longestSession = completedSessions.maxOfOrNull { it.getTotalDuration() } ?: 0
        
        // Calculate streak (consecutive days with sessions)
        val streak = calculateStreak(sessions)
        
        return SessionStatistics(
            totalSessions = completedSessions.size,
            totalDuration = totalDuration,
            averageDuration = averageDuration,
            longestSession = longestSession,
            currentStreak = streak
        )
    }
    
    private fun calculateStreak(sessions: List<BlockedProfileSessionEntity>): Int {
        if (sessions.isEmpty()) return 0
        
        // Group sessions by day
        val sessionsByDay = sessions
            .groupBy { session ->
                val dayInMillis = 24 * 60 * 60 * 1000L
                session.startTime / dayInMillis
            }
            .keys
            .sorted()
            .reversed()
        
        if (sessionsByDay.isEmpty()) return 0
        
        // Calculate consecutive days
        var streak = 1
        var previousDay = sessionsByDay.first()
        
        for (i in 1 until sessionsByDay.size) {
            val currentDay = sessionsByDay[i]
            if (previousDay - currentDay == 1L) {
                streak++
                previousDay = currentDay
            } else {
                break
            }
        }
        
        return streak
    }
}

data class SessionStatistics(
    val totalSessions: Int = 0,
    val totalDuration: Long = 0,
    val averageDuration: Long = 0,
    val longestSession: Long = 0,
    val currentStreak: Int = 0
)
