package citu.edu.stathis.mobile.features.vitals.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import citu.edu.stathis.mobile.core.theme.BrandColors
import citu.edu.stathis.mobile.features.vitals.data.model.HealthRiskAlert
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Info
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsScreen(
    viewModel: VitalsViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val realTimeState by viewModel.realTimeState.collectAsStateWithLifecycle()
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()
    val healthConnectState by viewModel.healthConnectState.collectAsStateWithLifecycle()
    val viewEvents by viewModel.events.collectAsStateWithLifecycle(initialValue = null)

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showActionableAlertDialog by remember { mutableStateOf<HealthRiskAlert?>(null) }


    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.requestHealthConnectPermissionsUseCase.createPermissionRequestContract(),
        onResult = { grantedPermissions ->
            viewModel.onPermissionsResult(grantedPermissions)
        }
    )

    LaunchedEffect(viewEvents) {
        viewEvents?.let { event ->
            when (event) {
                is VitalsViewEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is VitalsViewEvent.RequestHealthConnectPermissions -> {
                    healthConnectPermissionLauncher.launch(viewModel.requestHealthConnectPermissionsUseCase.getPermissionsSet())
                }
                is VitalsViewEvent.HandleAlertAction -> {
                    showActionableAlertDialog = event.alert
                }
            }
        }
    }

    if (showActionableAlertDialog != null) {
        ActionableHealthAlertDialog(
            alert = showActionableAlertDialog!!,
            onDismiss = { showActionableAlertDialog = null },
            onAction = {
                viewModel.handleAlertAction(showActionableAlertDialog!!)
                showActionableAlertDialog = null
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedTabIndex == 0 && healthConnectState is HealthConnectUiState.AvailableAndConnected && realTimeState is VitalsRealTimeUiState.Data) {
                FloatingActionButton(
                    onClick = { viewModel.saveCurrentVitals() },
                    containerColor = BrandColors.Purple
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Save Vitals", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HealthConnectStatusCard(
                healthConnectState = healthConnectState,
                onGrantPermissionsClick = {
                    healthConnectPermissionLauncher.launch(viewModel.requestHealthConnectPermissionsUseCase.getPermissionsSet())
                },
                onConnectClick = { viewModel.connectToHealthService() },
                onRefreshClick = { viewModel.triggerVitalsRefresh() }
            )

            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Real-time") },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Real-time Vitals") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("History") },
                    icon = { Icon(Icons.Default.History, contentDescription = "Vitals History") }
                )
            }

            when (selectedTabIndex) {
                0 -> RealTimeVitalsTabContent(
                    realTimeState = realTimeState,
                    healthConnectState = healthConnectState,
                    onConnectClick = { viewModel.connectToHealthService() },
                    onGrantPermissionsClick = { healthConnectPermissionLauncher.launch(viewModel.requestHealthConnectPermissionsUseCase.getPermissionsSet()) }
                )
                1 -> VitalsHistoryTabContent(
                    historyState = historyState,
                    onDeleteClick = { recordId -> viewModel.deleteVitalRecord(recordId) },
                    onRefreshClick = { viewModel.loadVitalsHistory()}
                )
            }
        }
    }
}

@Composable
fun HealthConnectStatusCard(
    healthConnectState: HealthConnectUiState,
    onGrantPermissionsClick: () -> Unit,
    onConnectClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Health Connect", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                when (healthConnectState) {
                    HealthConnectUiState.Initial, HealthConnectUiState.Connecting -> {
                        Text("Status: Checking / Connecting...")
                    }
                    HealthConnectUiState.ClientNotAvailable -> {
                        Text("Status: App Not Available", color = MaterialTheme.colorScheme.error)
                    }
                    HealthConnectUiState.PermissionsNotGranted -> {
                        Text("Status: Permissions Needed", color = MaterialTheme.colorScheme.error)
                    }
                    HealthConnectUiState.AvailableButDisconnected -> {
                        Text("Status: Ready to Connect")
                    }
                    HealthConnectUiState.AvailableAndConnected -> {
                        Text("Status: Connected", color = Color(0xFF4CAF50))
                    }
                    is HealthConnectUiState.Error -> {
                        Text("Status: Error - ${healthConnectState.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            when (healthConnectState) {
                HealthConnectUiState.PermissionsNotGranted -> {
                    Button(onClick = onGrantPermissionsClick) { Text("Grant") }
                }
                HealthConnectUiState.AvailableButDisconnected, is HealthConnectUiState.Error -> {
                    Button(onClick = onConnectClick) { Text("Connect") }
                }
                HealthConnectUiState.AvailableAndConnected -> {
                    IconButton(onClick = onRefreshClick) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh Vitals")
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun RealTimeVitalsTabContent(
    realTimeState: VitalsRealTimeUiState,
    healthConnectState: HealthConnectUiState,
    onConnectClick: () -> Unit,
    onGrantPermissionsClick: () -> Unit
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), contentAlignment = Alignment.TopCenter) {
        when (healthConnectState) {
            HealthConnectUiState.ClientNotAvailable -> InfoMessageScreen("Health Connect app is not installed or available on this device.")
            HealthConnectUiState.PermissionsNotGranted -> InfoMessageScreen("This feature requires Health Connect permissions.", "Grant Permissions", onGrantPermissionsClick)
            HealthConnectUiState.AvailableButDisconnected -> InfoMessageScreen("Not connected to Health Connect.", "Connect", onConnectClick)
            HealthConnectUiState.Connecting -> Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Text("Connecting to Health Connect...") }
            is HealthConnectUiState.Error -> InfoMessageScreen("Error: ${healthConnectState.message}", "Retry Connection", onConnectClick)
            HealthConnectUiState.AvailableAndConnected, HealthConnectUiState.Initial -> {
                when (realTimeState) {
                    VitalsRealTimeUiState.Initial -> Text("Initializing real-time vitals...")
                    VitalsRealTimeUiState.Loading -> CircularProgressIndicator()
                    is VitalsRealTimeUiState.Data -> {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Live Vitals", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
                            VitalsDisplayCards(vitalSigns = realTimeState.vitalSigns)
                            realTimeState.alert?.let {
                                Spacer(modifier = Modifier.height(16.dp))
                                HealthRiskAlertCard(alert = it)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Graph Placeholder: A dynamic graph of vitals (e.g., heart rate over time) would be shown here, updating periodically.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline)
                                    .padding(16.dp)
                            )
                        }
                    }
                    is VitalsRealTimeUiState.NoData -> InfoMessageScreen(realTimeState.message)
                    is VitalsRealTimeUiState.Error -> InfoMessageScreen("Error: ${realTimeState.message}", "Retry", onConnectClick)
                }
            }
        }
    }
}

@Composable
fun InfoMessageScreen(message: String, buttonText: String? = null, onButtonClick: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Info, contentDescription = "Info", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        buttonText?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onButtonClick?.invoke() }) {
                Text(it)
            }
        }
    }
}


@Composable
fun VitalsHistoryTabContent(
    historyState: VitalsHistoryUiState,
    onDeleteClick: (String) -> Unit,
    onRefreshClick: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        OutlinedButton(onClick = onRefreshClick, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Filled.Refresh, contentDescription = "Refresh History", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Refresh History")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            when (historyState) {
                VitalsHistoryUiState.Loading -> CircularProgressIndicator()
                VitalsHistoryUiState.Empty -> Text("No vitals history available. Saved vitals will appear here.")
                is VitalsHistoryUiState.Data -> {
                    if (historyState.vitalsList.isEmpty()){
                        Text("No vitals history available. Saved vitals will appear here.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(historyState.vitalsList) { vital ->
                                VitalHistoryItemCard(vitalSign = vital, onDeleteClick = {
                                    vital.id?.let { id -> onDeleteClick(id) }
                                })
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                is VitalsHistoryUiState.Error -> Text("Error fetching history: ${historyState.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun VitalsDisplayCards(vitalSigns: VitalSigns) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        vitalSigns.heartRate?.let {
            VitalDataCard(label = "Heart Rate", value = "$it BPM", icon = Icons.Filled.Favorite)
        }
        vitalSigns.oxygenSaturation?.let {
            VitalDataCard(label = "Oxygen Saturation", value = String.format(Locale.US, "%.1f %%", it), icon = Icons.Filled.Favorite)
        }
        if (vitalSigns.systolicBP != null && vitalSigns.diastolicBP != null) {
            VitalDataCard(label = "Blood Pressure", value = "${vitalSigns.systolicBP}/${vitalSigns.diastolicBP} mmHg", icon = Icons.Filled.Favorite)
        }
        vitalSigns.temperature?.let {
            VitalDataCard(label = "Temperature", value = String.format(Locale.US, "%.1f °C", it), icon = Icons.Filled.Favorite)
        }
        vitalSigns.respirationRate?.let {
            VitalDataCard(label = "Respiration Rate", value = "$it breaths/min", icon = Icons.Filled.Favorite)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Last updated: ${vitalSigns.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}", style = MaterialTheme.typography.bodySmall)
        vitalSigns.deviceName?.let {
            Text("Source: $it", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun VitalDataCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
            }
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun HealthRiskAlertCard(alert: HealthRiskAlert, onActionClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = "Alert", tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(alert.riskType, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            alert.suggestedAction?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Suggestion: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
            }
            if (onActionClick != null && alert.suggestedAction != null && alert.suggestedAction.contains("Pause exercise", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Pause Exercise", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}

@Composable
fun ActionableHealthAlertDialog(
    alert: HealthRiskAlert,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.NotificationsActive, contentDescription = "Alert") },
        title = { Text(text = alert.riskType, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(alert.message)
                alert.suggestedAction?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Suggestion: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (alert.suggestedAction != null && alert.suggestedAction.contains("Pause exercise", ignoreCase = true)) {
                Button(
                    onClick = {
                        onAction()
                        onDismiss()
                    }
                ) {
                    Text("Pause Exercise")
                }
            } else {
                Button(onClick = onDismiss) { Text("Acknowledge") }
            }
        },
        dismissButton = {
            if (alert.suggestedAction != null && alert.suggestedAction.contains("Pause exercise", ignoreCase = true)) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    )
}


@Composable
fun VitalHistoryItemCard(vitalSign: VitalSigns, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    vitalSign.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                if (vitalSign.id != null) {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete record", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                vitalSign.heartRate?.let { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("HR", style = MaterialTheme.typography.labelSmall); Text("$it BPM", fontWeight = FontWeight.SemiBold) } }
                vitalSign.oxygenSaturation?.let { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("SpO2", style = MaterialTheme.typography.labelSmall); Text(String.format(Locale.US, "%.1f %%", it), fontWeight = FontWeight.SemiBold) } }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                if (vitalSign.systolicBP != null && vitalSign.diastolicBP != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("BP", style = MaterialTheme.typography.labelSmall); Text("${vitalSign.systolicBP}/${vitalSign.diastolicBP}", fontWeight = FontWeight.SemiBold) }
                }
                vitalSign.temperature?.let { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Temp", style = MaterialTheme.typography.labelSmall); Text(String.format(Locale.US, "%.1f°C", it), fontWeight = FontWeight.SemiBold) } }
                vitalSign.respirationRate?.let { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Resp", style = MaterialTheme.typography.labelSmall); Text("$it rpm", fontWeight = FontWeight.SemiBold) } }
            }
        }
    }
}
