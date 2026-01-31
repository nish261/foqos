package com.foqos.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.foqos.presentation.components.GradientPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavController,
    viewModel: ProfileEditViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val availableApps by viewModel.availableApps.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()
    val selectedStrategy by viewModel.selectedStrategy.collectAsState()
    val domains by viewModel.domains.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var profileName by remember { mutableStateOf(profile?.name ?: "") }
    var showStrategyDialog by remember { mutableStateOf(false) }
    var showDomainDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Navigate back when saved
    LaunchedEffect(uiState) {
        if (uiState is ProfileEditUiState.Saved) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (profile == null) "Create Profile" else "Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveProfile(profileName) },
                        enabled = profileName.isNotBlank() && selectedApps.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Name
            item {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profile Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Gradient Picker
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        GradientPicker(
                            selectedGradientId = profile?.gradientId ?: 0,
                            onGradientSelected = { viewModel.setGradient(it) }
                        )
                    }
                }
            }

            // Strategy Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showStrategyDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Blocking Strategy", style = MaterialTheme.typography.titleMedium)
                            Text(
                                selectedStrategy.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(Icons.Filled.ArrowForward, "Change")
                    }
                }
            }

            // Apps Section Header with Allow Mode Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (profile?.appsAllowMode == true)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (profile?.appsAllowMode == true) "Apps to Allow (${selectedApps.size})" else "Apps to Block (${selectedApps.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (profile?.appsAllowMode == true) "Only these apps will be allowed" else "These apps will be blocked",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = profile?.appsAllowMode ?: false,
                            onCheckedChange = { viewModel.toggleAppsAllowMode() }
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search apps") },
                    leadingIcon = { Icon(Icons.Filled.Search, "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // App List
            items(
                availableApps.filter {
                    searchQuery.isBlank() || it.appName.contains(searchQuery, ignoreCase = true)
                }
            ) { appInfo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.toggleApp(appInfo.packageName) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = appInfo.icon.toBitmap(48, 48).asImageBitmap(),
                            contentDescription = appInfo.appName,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(appInfo.appName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                appInfo.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Checkbox(
                            checked = selectedApps.contains(appInfo.packageName),
                            onCheckedChange = null
                        )
                    }
                }
            }

            // Domains Section Header with Allow Mode Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (profile?.domainsAllowMode == true)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (profile?.domainsAllowMode == true) "Websites to Allow (${domains.size})" else "Websites to Block (${domains.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (profile?.domainsAllowMode == true) "Only these websites will be allowed" else "These websites will be blocked",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = profile?.domainsAllowMode ?: false,
                            onCheckedChange = { viewModel.toggleDomainsAllowMode() }
                        )
                    }
                }
            }

            // Add Domain Button
            item {
                Button(
                    onClick = { showDomainDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Website")
                }
            }

            // Domain List
            items(domains) { domain ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(domain, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { viewModel.removeDomain(domain) }) {
                            Icon(Icons.Filled.Delete, "Remove")
                        }
                    }
                }
            }
        }
    }

    // Strategy Selection Dialog
    if (showStrategyDialog) {
        AlertDialog(
            onDismissRequest = { showStrategyDialog = false },
            title = { Text("Choose Blocking Strategy") },
            text = {
                Column {
                    com.foqos.domain.model.BlockingStrategy.values().forEach { strategy ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                viewModel.setStrategy(strategy)
                                showStrategyDialog = false
                            }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(strategy.name, fontWeight = FontWeight.Bold)
                                Text(
                                    strategy.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStrategyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Domain Dialog
    if (showDomainDialog) {
        var newDomain by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDomainDialog = false },
            title = { Text("Add Website") },
            text = {
                OutlinedTextField(
                    value = newDomain,
                    onValueChange = { newDomain = it },
                    label = { Text("Domain (e.g., facebook.com)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addDomain(newDomain)
                        showDomainDialog = false
                    },
                    enabled = newDomain.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDomainDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
