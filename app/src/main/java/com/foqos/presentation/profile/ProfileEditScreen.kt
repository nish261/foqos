package com.foqos.presentation.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

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
    val nfcTags by viewModel.nfcTags.collectAsState()
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

            // NFC Tag Management
            profile?.let { currentProfile ->
                if (selectedStrategy.id == "nfc") {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate("nfc_tags/${currentProfile.id}") }
                        ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("NFC Tags", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${nfcTags.size} tag(s) configured",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(Icons.Filled.Nfc, "Manage NFC Tags")
                        }
                    }
                }
                }

                // STRICT UNLOCKS Section (only for existing profiles with NFC strategy)
                if (selectedStrategy.id == "nfc") {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "STRICT UNLOCKS",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Nfc,
                                                contentDescription = "NFC Tag",
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                "NFC Tag",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (currentProfile.strictUnlockTagId != null)
                                                "Set a specific NFC tag that can only unlock this profile when active"
                                            else
                                                "Set a specific NFC tag that can only unlock this profile when active",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { navController.navigate("write_nfc_tag/${currentProfile.id}") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Set", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (currentProfile.strictUnlockTagId != null) "Change" else "Set")
                                }

                                if (currentProfile.strictUnlockTagId != null) {
                                    Text(
                                        "Tag ID: ${currentProfile.strictUnlockTagId.take(8)}...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // App Selection Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Apps to Block (${selectedApps.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Search
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
            val filteredApps = if (searchQuery.isBlank()) {
                availableApps
            } else {
                availableApps.filter {
                    it.appName.contains(searchQuery, ignoreCase = true)
                }
            }
            
            items(filteredApps, key = { it.packageName }) { app ->
                AppSelectionItem(
                    app = app,
                    isSelected = selectedApps.contains(app.packageName),
                    onToggle = { viewModel.toggleApp(app.packageName) }
                )
            }
            
            // Website Blocking Section
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
) {
                    Text(
                        "Website Blocking (${domains.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showDomainDialog = true }) {
                        Icon(Icons.Filled.Add, "Add Domain")
                    }
                }
            }
            
            // Domain List
            items(domains) { domain ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(domain, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removeDomain(domain) }) {
                            Icon(
                                Icons.Filled.Delete,
                                "Remove",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Strategy Selection Dialog
    if (showStrategyDialog) {
        StrategySelectionDialog(
            currentStrategy = selectedStrategy,
            onDismiss = { showStrategyDialog = false },
            onSelect = { strategy ->
                viewModel.setStrategy(strategy)
                showStrategyDialog = false
            }
        )
    }
    
    // Add Domain Dialog
    if (showDomainDialog) {
        AddDomainDialog(
            onDismiss = { showDomainDialog = false },
            onAdd = { domain ->
                viewModel.addDomain(domain)
                showDomainDialog = false
            }
        )
    }
}

@Composable
private fun AppSelectionItem(
    app: com.foqos.util.AppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(200),
        label = "appCardColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = tween(200),
        label = "appCardElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation)
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val bitmap = remember(app.icon) { app.icon.toBitmap().asImageBitmap() }
            Image(
                bitmap = bitmap,
                contentDescription = app.appName,
                modifier = Modifier.size(40.dp)
            )
            Text(
                app.appName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun StrategySelectionDialog(
    currentStrategy: com.foqos.domain.model.BlockingStrategy,
    onDismiss: () -> Unit,
    onSelect: (com.foqos.domain.model.BlockingStrategy) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Blocking Strategy") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                com.foqos.domain.model.BlockingStrategy.getAll().forEach { strategy ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(strategy) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (strategy.id == currentStrategy.id) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                strategy.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                strategy.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun AddDomainDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var domain by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Website Domain") },
        text = {
            Column {
                Text("Enter domain to block (e.g., twitter.com)")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it.lowercase() },
                    label = { Text("Domain") },
                    singleLine = true,
                    placeholder = { Text("example.com") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (domain.isNotBlank()) onAdd(domain) },
                enabled = domain.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
