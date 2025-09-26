package citu.edu.stathis.mobile.features.vitals.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.features.vitals.data.HealthConnectManager
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import kotlinx.coroutines.delay

@Composable
fun HealthConnectScreen(
    navController: NavHostController,
    viewModel: HealthConnectViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val vitalSigns by viewModel.vitalSigns.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val cachedVitals by viewModel.cachedVitals.collectAsState()

    // Start monitoring when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.startMonitoring()
    }

    // Stop monitoring when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopMonitoring()
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Health Connect",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Watch Icon with Connection Status
            WatchIcon(
                isConnected = connectionState == HealthConnectManager.ConnectionState.CONNECTED,
                isMonitoring = isMonitoring,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Connection Status Text
            Text(
                text = when (connectionState) {
                    HealthConnectManager.ConnectionState.CONNECTED -> "Connected to Health Connect"
                    HealthConnectManager.ConnectionState.CONNECTING -> "Connecting..."
                    HealthConnectManager.ConnectionState.DISCONNECTED -> "Not Connected"
                    HealthConnectManager.ConnectionState.UNAVAILABLE -> "Health Connect Unavailable"
                },
                style = MaterialTheme.typography.titleMedium,
                color = when (connectionState) {
                    HealthConnectManager.ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
                    HealthConnectManager.ConnectionState.CONNECTING -> MaterialTheme.colorScheme.tertiary
                    HealthConnectManager.ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.error
                    HealthConnectManager.ConnectionState.UNAVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Monitoring Status
            if (isMonitoring) {
                Text(
                    text = "Monitoring vitals in real-time...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Current Vitals Display
            if (vitalSigns != null) {
                VitalsDisplayCard(vitalSigns!!)
            } else if (cachedVitals != null) {
                CachedVitalsDisplayCard(cachedVitals!!)
            } else {
                NoVitalsCard()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.connect() },
                    modifier = Modifier.weight(1f),
                    enabled = connectionState != HealthConnectManager.ConnectionState.CONNECTING
                ) {
                    Text("Connect")
                }
                
                OutlinedButton(
                    onClick = { viewModel.disconnect() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Disconnect")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WatchIcon(
    isConnected: Boolean,
    isMonitoring: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isConnected) 1.1f else 1f,
        animationSpec = tween(300),
        label = "watch_scale"
    )

    val pulseScale by animateFloatAsState(
        targetValue = if (isMonitoring && isConnected) 1.2f else 1f,
        animationSpec = tween(1000),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer pulse ring (only when monitoring)
        if (isMonitoring && isConnected) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
            )
        }

        // Main watch body
        Card(
            modifier = Modifier
                .size(100.dp)
                .scale(scale),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Watch face
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Watch hands (simplified)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConnected) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Center dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isConnected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VitalsDisplayCard(vitalSigns: VitalSigns) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Current Vitals",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VitalsItem(
                    icon = Icons.Filled.Favorite,
                    label = "Heart Rate",
                    value = "${vitalSigns.heartRate} BPM",
                    color = MaterialTheme.colorScheme.error
                )
                
                VitalsItem(
                    icon = Icons.Filled.Thermostat,
                    label = "Temperature",
                    value = "${String.format("%.1f", vitalSigns.temperature)}°C",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VitalsItem(
                    icon = Icons.Filled.WaterDrop,
                    label = "Oxygen",
                    value = "${String.format("%.1f", vitalSigns.oxygenSaturation)}%",
                    color = MaterialTheme.colorScheme.primary
                )
                
                VitalsItem(
                    icon = Icons.Filled.Favorite,
                    label = "BP",
                    value = "${vitalSigns.systolicBP}/${vitalSigns.diastolicBP}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Last updated: ${vitalSigns.timestamp.toString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CachedVitalsDisplayCard(vitalSigns: VitalSigns) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cached Vitals",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(Offline)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VitalsItem(
                    icon = Icons.Filled.Favorite,
                    label = "Heart Rate",
                    value = "${vitalSigns.heartRate} BPM",
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
                
                VitalsItem(
                    icon = Icons.Filled.Thermostat,
                    label = "Temperature",
                    value = "${String.format("%.1f", vitalSigns.temperature)}°C",
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VitalsItem(
                    icon = Icons.Filled.WaterDrop,
                    label = "Oxygen",
                    value = "${String.format("%.1f", vitalSigns.oxygenSaturation)}%",
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
                
                VitalsItem(
                    icon = Icons.Filled.Favorite,
                    label = "BP",
                    value = "${vitalSigns.systolicBP}/${vitalSigns.diastolicBP}",
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Cached: ${vitalSigns.timestamp.toString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NoVitalsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Vitals Data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Connect to Health Connect to start monitoring your vital signs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VitalsItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
