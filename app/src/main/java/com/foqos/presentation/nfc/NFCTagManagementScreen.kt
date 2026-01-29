package com.foqos.presentation.nfc

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.foqos.domain.model.NFCTagConfig
import com.foqos.domain.model.NFCTagMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCTagManagementScreen(
    navController: NavController,
    profileId: String,
    viewModel: NFCTagManagementViewModel = hiltViewModel()
) {
    val nfcTags by viewModel.nfcTags.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf<NFCTagMode?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadTags(profileId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFC Tag Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, "Add Tag") },
                text = { Text("Add NFC Tag") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scanning indicator with pulse animation
            AnimatedVisibility(
                visible = isScanning,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "scan")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .shadow(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Column {
                            Text(
                                "📡 Scanning for NFC tag...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Hold your NFC tag near the device",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "About NFC Tags",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Configure NFC tags with different modes:\n\n" +
                        "• UNLOCK: End the current session\n" +
                        "• PAUSE: Temporarily pause blocking\n" +
                        "• RESUME: Resume blocking after pause\n" +
                        "• EMERGENCY: Bypass cooldowns\n" +
                        "• REMOTE LOCK: Toggle remote lock",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Tags list
            if (nfcTags.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Nfc,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "No NFC tags configured",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Tap the + button to add a tag",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(nfcTags, key = { it.tagId }) { tag ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 })
                        ) {
                            NFCTagCard(
                                tag = tag,
                                onDelete = { viewModel.removeTag(profileId, tag.tagId) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add tag dialog
    if (showAddDialog) {
        AddNFCTagDialog(
            onDismiss = { showAddDialog = false },
            onModeSelected = { mode ->
                selectedMode = mode
                showAddDialog = false
                viewModel.startScanning(profileId, mode)
            }
        )
    }
}

@Composable
private fun NFCTagCard(
    tag: NFCTagConfig,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
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
                        NFCTagMode.CUSTOM -> Icons.Filled.Settings
                    },
                    contentDescription = tag.mode.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        tag.label ?: tag.mode.name.replace("_", " "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        tag.mode.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "ID: ${tag.tagId.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
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
    onDismiss: () -> Unit,
    onModeSelected: (NFCTagMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select NFC Tag Mode") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NFCTagMode.values()) { mode ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = { onModeSelected(mode) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (mode) {
                                    NFCTagMode.UNLOCK -> Icons.Filled.LockOpen
                                    NFCTagMode.PAUSE -> Icons.Filled.Pause
                                    NFCTagMode.RESUME -> Icons.Filled.PlayArrow
                                    NFCTagMode.EMERGENCY -> Icons.Filled.Warning
                                    NFCTagMode.REMOTE_LOCK_TOGGLE -> Icons.Filled.Lock
                                    NFCTagMode.CUSTOM -> Icons.Filled.Settings
                                },
                                contentDescription = mode.name,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    mode.name.replace("_", " "),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    when (mode) {
                                        NFCTagMode.UNLOCK -> "End current session"
                                        NFCTagMode.PAUSE -> "Temporarily pause blocking"
                                        NFCTagMode.RESUME -> "Resume blocking"
                                        NFCTagMode.EMERGENCY -> "Bypass all cooldowns"
                                        NFCTagMode.REMOTE_LOCK_TOGGLE -> "Toggle remote lock"
                                        NFCTagMode.CUSTOM -> "Custom action (not yet implemented)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
