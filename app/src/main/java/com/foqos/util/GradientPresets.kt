package com.foqos.util

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object GradientPresets {
    private val gradients = listOf(
        // 0: Purple → Blue
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF6366F1), // Indigo
                Color(0xFF3B82F6)  // Blue
            )
        ),
        // 1: Orange → Pink
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF97316), // Orange
                Color(0xFFEC4899)  // Pink
            )
        ),
        // 2: Green → Teal
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF10B981), // Green
                Color(0xFF14B8A6)  // Teal
            )
        ),
        // 3: Red → Orange
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFEF4444), // Red
                Color(0xFFF97316)  // Orange
            )
        ),
        // 4: Blue → Purple
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF3B82F6), // Blue
                Color(0xFF8B5CF6)  // Purple
            )
        ),
        // 5: Yellow → Green
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFBBF24), // Yellow
                Color(0xFF10B981)  // Green
            )
        ),
        // 6: Pink → Purple
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFEC4899), // Pink
                Color(0xFF8B5CF6)  // Purple
            )
        ),
        // 7: Teal → Blue
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF14B8A6), // Teal
                Color(0xFF0EA5E9)  // Sky Blue
            )
        )
    )

    fun getGradient(index: Int): Brush {
        return gradients.getOrNull(index) ?: gradients[0]
    }

    fun getAllGradients(): List<Brush> = gradients
}
