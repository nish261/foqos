package com.foqos.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foqos.domain.model.NFCTagConfig
import com.foqos.domain.model.NFCTagMode

/**
 * Screen for managing multiple NFC tags with different modes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCTagManagementScreen(
    tags: List<NFCTagConfig>,
    onAddTag: (NFCTagMode, String?) -> Unit,
    onRemoveTag: (String) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFC Tag Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, "Add Tag")
                    }
                }
            )
        }
    ) { padding ->
        if (tags.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Nfc,
                        contentDescription = "NFC",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "No NFC Tags Configured",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tap + to add an NFC tag",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tags, key = { it.tagId }) { tag ->
                    NFCTagCard(
                        tag = tag,
                        onRemove = { onRemoveTag(tag.tagId) }
                    )
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddNFCTagDialog(
            onAdd = { mode, label ->
                showAddDialog = false
                // This would trigger NFC scanning
                onAddTag(mode, label)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun NFCTagCard(
    tag: NFCTagConfig,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (tag.mode) {
                NFCTagMode.UNLOCK -> MaterialTheme.colorScheme.primaryContainer
                NFCTagMode.EMERGENCY -> MaterialTheme.colorScheme.errorContainer
                NFCTagMode.REMOTE_LOCK_TOGGLE -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (tag.mode) {
                    NFCTagMode.UNLOCK -> Icons.Filled.LockOpen
                    NFCTagMode.PAUSE -> Icons.Filled.Pause
                    NFCTagMode.RESUME -> Icons.Filled.PlayArrow
                    NFCTagMode.EMERGENCY -> Icons.Filled.Warning
                    NFCTagMode.REMOTE_LOCK_TOGGLE -> Icons.Filled.Lock
                    else -> Icons.Filled.Nfc
                },
                contentDescription = tag.mode.name,
                modifier = Modifier.size(32.dp),
                tint = when (tag.mode) {
                    NFCTagMode.UNLOCK -> MaterialTheme.colorScheme.primary
                    NFCTagMode.EMERGENCY -> MaterialTheme.colorScheme.error
                    NFCTagMode.REMOTE_LOCK_TOGGLE -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tag.label ?: tag.mode.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    tag.mode.getDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    "ID: ${tag.tagId.takeLast(8)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Delete,
                    "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddNFCTagDialog(
    onAdd: (NFCTagMode, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(NFCTagMode.UNLOCK) }
    var label by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.Nfc, "NFC", modifier = Modifier.size(48.dp))
        },
        title = { Text("Add NFC Tag") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Choose the purpose for this NFC tag:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Mode selection chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NFCTagMode.values().filter { it != NFCTagMode.CUSTOM }.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(mode.getDisplayName()) },
                            leadingIcon = if (selectedMode == mode) {
                                { Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
                
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    placeholder = { Text("e.g., 'Desk Tag', 'Emergency Tag'") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(
                        "ℹ️ ${selectedMode.getDescription()}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(selectedMode, label.takeIf { it.isNotBlank() }) }) {
                Text("Scan NFC Tag")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun NFCTagMode.getDisplayName(): String = when (this) {
    NFCTagMode.UNLOCK -> "Unlock Session"
    NFCTagMode.PAUSE -> "Pause Session"
    NFCTagMode.RESUME -> "Resume Session"
    NFCTagMode.EMERGENCY -> "Emergency Unlock"
    NFCTagMode.REMOTE_LOCK_TOGGLE -> "Remote Lock Toggle"
    NFCTagMode.CUSTOM -> "Custom Action"
}

private fun NFCTagMode.getDescription(): String = when (this) {
    NFCTagMode.UNLOCK -> "Ends the active session completely"
    NFCTagMode.PAUSE -> "Temporarily pauses the active session"
    NFCTagMode.RESUME -> "Resumes a paused session"
    NFCTagMode.EMERGENCY -> "Emergency bypass - use sparingly!"
    NFCTagMode.REMOTE_LOCK_TOGGLE -> "Enables/disables remote lock mode"
    NFCTagMode.CUSTOM -> "Custom action for future use"
}
