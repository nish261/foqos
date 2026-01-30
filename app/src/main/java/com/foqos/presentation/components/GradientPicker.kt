package com.foqos.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GradientPicker(
    selectedGradientId: Int,
    onGradientSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Profile Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Choose a gradient color for your profile card",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed((0..7).toList()) { index, gradientId ->
                GradientOption(
                    gradientId = gradientId,
                    isSelected = gradientId == selectedGradientId,
                    onClick = { onGradientSelected(gradientId) }
                )
            }
        }
    }
}

@Composable
private fun GradientOption(
    gradientId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val gradient = GradientPresets.getGradient(gradientId)
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = tween(200),
        label = "borderWidth"
    )
    val size by animateDpAsState(
        targetValue = if (isSelected) 60.dp else 56.dp,
        animationSpec = tween(200),
        label = "size"
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = borderWidth,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .background(gradient, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GradientPickerDialog(
    selectedGradientId: Int,
    onDismiss: () -> Unit,
    onGradientSelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Profile Color") },
        text = {
            GradientPicker(
                selectedGradientId = selectedGradientId,
                onGradientSelected = { gradientId ->
                    onGradientSelected(gradientId)
                    onDismiss()
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
