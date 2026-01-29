package com.foqos.presentation.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HoldToStartButton(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isHolding by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    // Animated progress that fills over 2 seconds
    val animatedProgress by animateFloatAsState(
        targetValue = if (isHolding) 1f else 0f,
        animationSpec = if (isHolding) {
            tween(durationMillis = 2000, easing = LinearEasing)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        },
        label = "holdProgress"
    )

    // Update progress and trigger callbacks
    LaunchedEffect(animatedProgress) {
        progress = animatedProgress

        // Haptic feedback at 50%
        if (progress >= 0.5f && progress < 0.6f && isHolding) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }

        // Trigger callback and haptic at 100%
        if (progress >= 0.99f && isHolding) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            isHolding = false
            onStart()
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isHolding) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                            // Wait for release
                            val released = tryAwaitRelease()
                            if (released && progress < 0.99f) {
                                isHolding = false
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Background button
        Button(
            onClick = {}, // Handled by gesture detector
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            enabled = enabled
        ) {
            Text(
                text = when {
                    !enabled -> "Disabled"
                    isHolding && progress > 0.5f -> "Starting..."
                    isHolding -> "Hold..."
                    else -> "Hold to Start"
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Progress indicator overlay
        if (isHolding || progress > 0f) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        }
    }
}
