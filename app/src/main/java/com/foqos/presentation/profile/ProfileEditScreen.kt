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
import com.foqos.presentation.components.ScheduleConfig
import com.foqos.presentation.components.ScheduleSetupDialog

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
    var showScheduleDialog by remember { mutableStateOf(false) }
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

            // Block All Browsers Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (profile?.blockAllBrowsers == true)
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
                                "Block All Browsers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Automatically blocks Chrome, Firefox, Safari, and all other browsers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = profile?.blockAllBrowsers ?: false,
                            onCheckedChange = { viewModel.toggleBlockAllBrowsers() }
                        )
                    }
                }
            }

            // Reminder System
            item {
                val reminderEnabled = profile?.reminderTimeInSeconds != null
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (reminderEnabled)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Focus Reminders",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Periodic notifications to help you stay focused during sessions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { viewModel.toggleReminders() }
                            )
                        }

                        if (reminderEnabled) {
                            // Time interval selector
                            Text(
                                "Reminder Interval",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val intervals = listOf(
                                    300 to "5 min",
                                    600 to "10 min",
                                    900 to "15 min",
                                    1800 to "30 min"
                                )
                                intervals.forEach { (seconds, label) ->
                                    FilterChip(
                                        selected = profile?.reminderTimeInSeconds == seconds,
                                        onClick = { viewModel.setReminderInterval(seconds) },
                                        label = { Text(label) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Custom message
                            OutlinedTextField(
                                value = profile?.customReminderMessage ?: "",
                                onValueChange = { viewModel.setReminderMessage(it) },
                                label = { Text("Custom Message (optional)") },
                                placeholder = { Text("You're doing great! Keep focusing...") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2,
                                supportingText = {
                                    Text(
                                        "Leave blank for default message",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Strict Mode
            item {
                val strictModeEnabled = profile?.enableStrictMode == true
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (strictModeEnabled)
                            MaterialTheme.colorScheme.errorContainer
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
                                "Strict Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Prevents app uninstallation and settings access during sessions. Requires Device Admin permission.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = strictModeEnabled,
                            onCheckedChange = { viewModel.toggleStrictMode() }
                        )
                    }
                }
            }

            // Disable Background Stops
            item {
                val disableBackgroundStops = profile?.disableBackgroundStops == true
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (disableBackgroundStops)
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
                                "Disable Background Stops",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Prevents system from stopping the app in background during sessions. May increase battery usage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = disableBackgroundStops,
                            onCheckedChange = { viewModel.toggleDisableBackgroundStops() }
                        )
                    }
                }
            }

            // Schedule System
            item {
                val scheduleEnabled = profile?.scheduleEnabled == true
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (scheduleEnabled)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Schedule",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Restrict sessions to specific days and times",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = scheduleEnabled,
                                onCheckedChange = { viewModel.toggleSchedule() }
                            )
                        }

                        if (scheduleEnabled) {
                            // Show current schedule
                            val days = profile?.scheduleDaysOfWeek?.mapNotNull { dayNum ->
                                when (dayNum) {
                                    1 -> "Mon"
                                    2 -> "Tue"
                                    3 -> "Wed"
                                    4 -> "Thu"
                                    5 -> "Fri"
                                    6 -> "Sat"
                                    7 -> "Sun"
                                    else -> null
                                }
                            }?.joinToString(", ") ?: "No days selected"

                            val timeRange = if (profile?.scheduleStartTime != null && profile?.scheduleEndTime != null) {
                                "${profile?.scheduleStartTime} - ${profile?.scheduleEndTime}"
                            } else {
                                "No time set"
                            }

                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { showScheduleDialog = true }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Days",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            days,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Time",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            timeRange,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "Tap to edit",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
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

            // App Selection Header with Allow Mode Toggle
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (profile?.appsAllowMode == true)
                                "Apps to Allow (${selectedApps.size})"
                            else
                                "Apps to Block (${selectedApps.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Allow Mode Toggle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (profile?.appsAllowMode == true) "Allow Mode" else "Block Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (profile?.appsAllowMode == true)
                                        "Only selected apps are allowed, all others blocked"
                                    else
                                        "Only selected apps are blocked, all others allowed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Switch(
                                checked = profile?.appsAllowMode ?: false,
                                onCheckedChange = { viewModel.toggleAppsAllowMode() }
                            )
                        }
                    }
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (profile?.domainsAllowMode == true)
                                "Domains to Allow (${domains.size})"
                            else
                                "Website Blocking (${domains.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showDomainDialog = true }) {
                            Icon(Icons.Filled.Add, "Add Domain")
                        }
                    }

                    // Domain Allow Mode Toggle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (profile?.domainsAllowMode == true) "Allow Mode" else "Block Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (profile?.domainsAllowMode == true)
                                        "Only selected domains are allowed, all others blocked"
                                    else
                                        "Only selected domains are blocked, all others allowed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Switch(
                                checked = profile?.domainsAllowMode ?: false,
                                onCheckedChange = { viewModel.toggleDomainsAllowMode() }
                            )
                        }
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

    // Schedule Setup Dialog
    if (showScheduleDialog) {
        val currentSchedule = if (profile?.scheduleEnabled == true &&
            profile?.scheduleDaysOfWeek != null &&
            profile?.scheduleStartTime != null &&
            profile?.scheduleEndTime != null) {
            ScheduleConfig(
                daysOfWeek = profile?.scheduleDaysOfWeek!!,
                startTime = profile?.scheduleStartTime!!,
                endTime = profile?.scheduleEndTime!!
            )
        } else null

        ScheduleSetupDialog(
            currentSchedule = currentSchedule,
            onDismiss = { showScheduleDialog = false },
            onSave = { schedule ->
                viewModel.setSchedule(schedule)
                showScheduleDialog = false
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
