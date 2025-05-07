package citu.edu.stathis.mobile.features.vitals.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.core.theme.BrandColors
import citu.edu.stathis.mobile.features.vitals.domain.model.VitalSigns
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsScreen(
    navController: NavHostController,
    viewModel: VitalsViewModel = hiltViewModel()
) {
    val TAG = "VitalsScreen"
    val uiState by viewModel.uiState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()
    val deviceState by viewModel.deviceState.collectAsState()
    val bluetoothState by viewModel.bluetoothState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showDeviceDialog by remember { mutableStateOf(false) }
    var showBluetoothDialog by remember { mutableStateOf(false) }
    var showDebugDialog by remember { mutableStateOf(false) }

    // Request BLE permissions
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            if (bluetoothState == BluetoothState.ENABLED) {
                viewModel.startScan()
                showDeviceDialog = true
            } else {
                showBluetoothDialog = true
            }
        } else {
            showPermissionDialog = true
        }
    }

    // Enable Bluetooth launcher
    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check if Bluetooth is enabled after the activity returns
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val isEnabled = bluetoothManager?.adapter?.isEnabled == true

        if (isEnabled) {
            // Bluetooth is now enabled, can start scanning
            viewModel.startScan()
            showDeviceDialog = true
        } else {
            // User didn't enable Bluetooth
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Bluetooth is required to connect to your device")
            }
        }
    }

    // Clean up when leaving the screen
    DisposableEffect(key1 = true) {
        onDispose {
            viewModel.stopScan()
            viewModel.disconnectDevice()
        }
    }

    // Show Bluetooth dialog if disabled
    LaunchedEffect(bluetoothState) {
        if (bluetoothState == BluetoothState.DISABLED) {
            showBluetoothDialog = true
        }
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTabIndex == 0 && uiState is VitalsUiState.Data) {
                FloatingActionButton(
                    onClick = { viewModel.saveCurrentVitals() },
                    containerColor = BrandColors.Purple,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Vitals"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with device connection status
            DeviceStatusHeader(
                deviceState = deviceState,
                onConnectClick = {
                    permissionLauncher.launch(permissionsToRequest)
                },
                onDisconnectClick = {
                    viewModel.disconnectDevice()
                },
                onDebugClick = {
                    showDebugDialog = true
                }
            )

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BrandColors.Purple
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Real-time") },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("History") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
            }

            // Content based on selected tab
            when (selectedTabIndex) {
                0 -> RealTimeVitalsContent(
                    uiState = uiState,
                    deviceState = deviceState,
                    onConnectClick = {
                        permissionLauncher.launch(permissionsToRequest)
                    }
                )
                1 -> VitalsHistoryContent(
                    historyState = historyState,
                    onDeleteClick = { id -> viewModel.deleteVitalRecord(id) }
                )
            }
        }

        // Permission dialog
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Bluetooth Permissions Required") },
                text = { Text("To connect to your Xiaomi Smart Band, we need Bluetooth permissions. Please grant them in your device settings.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionDialog = false
                            permissionLauncher.launch(permissionsToRequest)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandColors.Purple
                        )
                    ) {
                        Text("Try Again")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Bluetooth dialog
        if (showBluetoothDialog) {
            AlertDialog(
                onDismissRequest = { showBluetoothDialog = false },
                title = { Text("Bluetooth is Disabled") },
                text = { Text("Bluetooth is required to connect to your Xiaomi Smart Band. Please enable Bluetooth.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showBluetoothDialog = false
                            // Request user to enable Bluetooth
                            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            bluetoothEnableLauncher.launch(enableBtIntent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandColors.Purple
                        )
                    ) {
                        Text("Enable Bluetooth")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBluetoothDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Device selection dialog
        if (showDeviceDialog) {
            DeviceSelectionDialog(
                deviceState = deviceState,
                onDismiss = {
                    showDeviceDialog = false
                    viewModel.stopScan()
                },
                onDeviceSelected = { device ->
                    viewModel.connectToDevice(device)
                    showDeviceDialog = false
                },
                onRescanClick = {
                    viewModel.startScan()
                }
            )
        }

        // Debug dialog
        if (showDebugDialog) {
            AlertDialog(
                onDismissRequest = { showDebugDialog = false },
                title = { Text("BLE Connection Debug") },
                text = {
                    Column {
                        Text("Bluetooth State: $bluetoothState")
                        Text("Device State: $deviceState")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("If you're having trouble connecting:", fontWeight = FontWeight.Bold)
                        Text("1. Make sure your device is nearby and charged")
                        Text("2. Try turning Bluetooth off and on again")
                        Text("3. Make sure your Xiaomi Smart Band is in pairing mode")
                        Text("4. Restart the app")
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Bluetooth Settings")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showDebugDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandColors.Purple
                        )
                    ) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun DeviceStatusHeader(
    deviceState: DeviceState,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onDebugClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                when (deviceState) {
                                    is DeviceState.Connected -> BrandColors.Purple.copy(alpha = 0.2f)
                                    is DeviceState.Connecting -> Color.Yellow.copy(alpha = 0.2f)
                                    is DeviceState.Scanning -> Color.Blue.copy(alpha = 0.2f)
                                    else -> Color.Gray.copy(alpha = 0.2f)
                                }
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (deviceState) {
                                is DeviceState.Connected -> Icons.Default.BluetoothConnected
                                is DeviceState.Connecting -> Icons.Default.BluetoothSearching
                                is DeviceState.Scanning -> Icons.Default.Bluetooth
                                else -> Icons.Default.BluetoothDisabled
                            },
                            contentDescription = "Device Status",
                            tint = when (deviceState) {
                                is DeviceState.Connected -> BrandColors.Purple
                                is DeviceState.Connecting -> Color(0xFFFFAA00)
                                is DeviceState.Scanning -> Color(0xFF2196F3)
                                else -> Color.Gray
                            }
                        )
                    }

                    Column(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = when (deviceState) {
                                is DeviceState.Connected -> "Connected"
                                is DeviceState.Connecting -> "Connecting..."
                                is DeviceState.Scanning -> "Scanning..."
                                is DeviceState.NoDevicesFound -> "No Devices Found"
                                else -> "Disconnected"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Xiaomi Smart Band 9 Active",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onDebugClick,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Debug",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (deviceState is DeviceState.Connected) {
                        OutlinedButton(
                            onClick = onDisconnectClick,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Disconnect")
                        }
                    } else {
                        Button(
                            onClick = onConnectClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandColors.Purple
                            )
                        ) {
                            Text("Connect")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealTimeVitalsContent(
    uiState: VitalsUiState,
    deviceState: DeviceState,
    onConnectClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            deviceState !is DeviceState.Connected && uiState !is VitalsUiState.Data -> {
                // Not connected and no data
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.BluetoothDisabled,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Connect to your Xiaomi Smart Band to view real-time vitals",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onConnectClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandColors.Purple
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect Device")
                    }
                }
            }
            deviceState is DeviceState.Connecting -> {
                // Connecting state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = BrandColors.Purple)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Connecting to your device...",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            uiState is VitalsUiState.Data -> {
                // Connected with data
                VitalsDataDisplay(vitalSigns = uiState.vitalSigns)
            }
            uiState is VitalsUiState.Error -> {
                // Error state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onConnectClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandColors.Purple
                        )
                    ) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

@Composable
fun VitalsDataDisplay(vitalSigns: VitalSigns) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Heart Rate
        VitalSignCard(
            title = "Heart Rate",
            value = "${vitalSigns.heartRate}",
            unit = "BPM",
            icon = Icons.Default.Favorite,
            color = Color(0xFFFF5252),
            progress = vitalSigns.heartRate.toFloat() / 200f // Normalized to max 200 BPM
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Blood Pressure
        VitalSignCard(
            title = "Blood Pressure",
            value = "${vitalSigns.systolicBP}/${vitalSigns.diastolicBP}",
            unit = "mmHg",
            icon = Icons.Default.WaterDrop,
            color = Color(0xFF2196F3),
            progress = vitalSigns.systolicBP.toFloat() / 180f // Normalized to max 180 mmHg
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Temperature
        VitalSignCard(
            title = "Temperature",
            value = String.format("%.1f", vitalSigns.temperature),
            unit = "°C",
            icon = Icons.Default.Thermostat,
            color = Color(0xFFFF9800),
            progress = (vitalSigns.temperature - 35f) / 5f // Normalized from 35-40°C
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Oxygen Saturation
        VitalSignCard(
            title = "Oxygen Saturation",
            value = String.format("%.1f", vitalSigns.oxygenSaturation),
            unit = "%",
            icon = Icons.Default.WaterDrop,
            color = Color(0xFF4CAF50),
            progress = vitalSigns.oxygenSaturation / 100f
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Respiration Rate
        VitalSignCard(
            title = "Respiration Rate",
            value = "${vitalSigns.respirationRate}",
            unit = "breaths/min",
            icon = Icons.Default.Favorite,
            color = Color(0xFF9C27B0),
            progress = vitalSigns.respirationRate.toFloat() / 30f // Normalized to max 30 breaths/min
        )
    }
}

@Composable
fun VitalSignCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progressAnimation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$value $unit",
                        style = MaterialTheme.typography.headlineSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun VitalsHistoryContent(
    historyState: VitalsHistoryState,
    onDeleteClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (historyState) {
            is VitalsHistoryState.Loading -> {
                CircularProgressIndicator(color = BrandColors.Purple)
            }
            is VitalsHistoryState.Empty -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No vitals history found",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Connect to your device and save vitals to see them here",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is VitalsHistoryState.Data -> {
                LazyColumn {
                    items(historyState.vitalsList) { vitalSigns ->
                        VitalHistoryItem(
                            vitalSigns = vitalSigns,
                            onDeleteClick = { vitalSigns.id?.let { onDeleteClick(it) } }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VitalHistoryItem(
    vitalSigns: VitalSigns,
    onDeleteClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vitalSigns.timestamp.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                VitalHistoryValue(
                    label = "Heart Rate",
                    value = "${vitalSigns.heartRate} BPM",
                    color = Color(0xFFFF5252)
                )

                VitalHistoryValue(
                    label = "BP",
                    value = "${vitalSigns.systolicBP}/${vitalSigns.diastolicBP}",
                    color = Color(0xFF2196F3)
                )

                VitalHistoryValue(
                    label = "O₂",
                    value = "${vitalSigns.oxygenSaturation}%",
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                VitalHistoryValue(
                    label = "Temp",
                    value = "${vitalSigns.temperature}°C",
                    color = Color(0xFFFF9800)
                )

                VitalHistoryValue(
                    label = "Resp",
                    value = "${vitalSigns.respirationRate}/min",
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
fun VitalHistoryValue(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DeviceSelectionDialog(
    deviceState: DeviceState,
    onDismiss: () -> Unit,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onRescanClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Device") },
        text = {
            Column {
                when (deviceState) {
                    is DeviceState.Scanning -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandColors.Purple)
                        }

                        Text(
                            text = "Scanning for devices...",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is DeviceState.ScannedDevices -> {
                        if (deviceState.devices.isEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFFAA00),
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "No devices found. Make sure your Xiaomi Smart Band is nearby and in pairing mode.",
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = onRescanClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandColors.Purple
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Scan Again")
                                }
                            }
                        } else {
                            Text(
                                text = "Available Devices:",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyColumn(
                                modifier = Modifier.height(300.dp)
                            ) {
                                items(deviceState.devices) { device ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onDeviceSelected(device) }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bluetooth,
                                            contentDescription = null,
                                            tint = BrandColors.Purple
                                        )

                                        Spacer(modifier = Modifier.size(16.dp))

                                        Column {
                                            Text(
                                                text = device.name ?: "Unknown Device",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Text(
                                                text = device.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Divider()
                                }
                            }
                        }
                    }
                    is DeviceState.NoDevicesFound -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFFAA00),
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No devices found. Make sure your Xiaomi Smart Band is nearby and in pairing mode.",
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onRescanClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandColors.Purple
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Again")
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = "Click the scan button to search for available devices.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onRescanClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandColors.Purple
                            ),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Scan")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColors.Purple
                )
            ) {
                Text("Close")
            }
        }
    )
}