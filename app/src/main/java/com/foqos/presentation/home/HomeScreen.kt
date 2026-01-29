package com.foqos.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.foqos.data.local.entity.BlockedProfileEntity
import com.foqos.presentation.components.ProfileCard
import com.foqos.presentation.components.ActiveSessionBanner
import com.foqos.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    
    // Handle UI State
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is HomeUiState.Success -> {
                // Could show a snackbar here
                viewModel.clearUiState()
            }
            is HomeUiState.Error -> {
                // Could show a snackbar here
                viewModel.clearUiState()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Foqos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (activeSession == null) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.ProfileEdit.createRoute(null)) },
                    icon = { Icon(Icons.Filled.Add, "Create Profile") },
                    text = { Text("New Profile") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Active session banner
            activeSession?.let { activeSession ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Remote lock banner if active
                    if (activeSession.remoteLockActivatedTime != null) {
                        RemoteLockBanner(
                            activatedTime = activeSession.remoteLockActivatedTime,
                            activatedBy = activeSession.remoteLockActivatedBy,
                            onShowNFCScanner = {
                                // Show NFC scanner
                            }
                        )
                    }
                    
                    // Normal session banner
                    ActiveSessionBanner(
                        session = activeSession,
                        onStop = { viewModel.stopSession() },
                        onBreak = { showBreakDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Emergency unlock and remote lock controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Emergency unlock button
                        OutlinedButton(
                            onClick = { showEmergencyDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = "Emergency",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Emergency")
                        }
                        
                        // Remote lock toggle button
                        if (activeSession.remoteLockActivatedTime == null) {
                            FilledTonalButton(
                                onClick = { showRemoteLockDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.Lock,
                                    contentDescription = "Remote Lock",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remote Lock")
                            }
                        }
                    }
                }
            }
            
            // Profile list
            if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Welcome to Foqos!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Create your first profile to get started",
                            style = MaterialTheme.typography.bodyLarge,
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
                    items(profiles, key = { it.id }) { profile ->
                        ProfileCard(
                            profile = profile,
                            isActive = activeSession?.profileId == profile.id,
                            onStart = { viewModel.startSession(profile) },
                            onDelete = { viewModel.deleteProfile(profile) }
                        )
                    }
                }
            }
        }
    }
    
    // Create profile dialog
    if (showCreateDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createProfile(name)
                showCreateDialog = false
            }
        )
    }

    // Dialogs for active session
    activeSession?.let { session ->
        val selectedProfile = profiles.find { it.id == session.profileId }
        
        if (showBreakDialog && selectedProfile != null) {
            BreakDialog(
                profile = selectedProfile,
                onDismiss = { showBreakDialog = false },
                onStartBreak = { duration ->
                    viewModel.startBreak(duration)
                    showBreakDialog = false
                }
            )
        }
        
        if (showEmergencyDialog && selectedProfile != null) {
            val attemptsRemaining = selectedProfile.emergencyUnlockAttempts - session.emergencyUnlockAttemptsUsed
            EmergencyUnlockDialog(
                attemptsRemaining = attemptsRemaining,
                maxAttempts = selectedProfile.emergencyUnlockAttempts,
                cooldownEndTime = session.emergencyUnlockCooldownUntil,
                onUnlock = {
                    viewModel.useEmergencyUnlock()
                    showEmergencyDialog = false
                },
                onDismiss = { showEmergencyDialog = false }
            )
        }
        
        if (showRemoteLockDialog) {
            RemoteLockActivationDialog(
                onActivate = { deviceName ->
                    viewModel.activateRemoteLock(deviceName)
                    showRemoteLockDialog = false
                },
                onDismiss = { showRemoteLockDialog = false }
            )
        }
    }
}

@Composable
fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Profile") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Profile Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
