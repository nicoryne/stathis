package citu.edu.stathis.mobile.features.vitals.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import citu.edu.stathis.mobile.features.vitals.ui.VitalsHistoryUiState

@Composable
fun VitalsHistoryTabContent(
    historyState: VitalsHistoryUiState,
    onRefreshClick: () -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vitals History",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = onRefreshClick) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh History")
            }
        }

        when (historyState) {
            is VitalsHistoryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is VitalsHistoryUiState.Data -> {
                val vitalsList = historyState.vitalsList
                if (vitalsList.isNotEmpty()) {
                    // Heart Rate Graph
                    VitalsGraph(
                        vitalsList = vitalsList,
                        title = "Heart Rate History",
                        valueSelector = { it.heartRate.toFloat() }
                    )

                    // Blood Oxygen Graph
                    VitalsGraph(
                        vitalsList = vitalsList,
                        title = "Blood Oxygen History",
                        valueSelector = { it.oxygenSaturation }
                    )

                    // Blood Pressure Graph (Systolic)
                    VitalsGraph(
                        vitalsList = vitalsList,
                        title = "Blood Pressure (Systolic) History",
                        valueSelector = { it.systolicBP.toFloat() }
                    )

                    // Blood Pressure Graph (Diastolic)
                    VitalsGraph(
                        vitalsList = vitalsList,
                        title = "Blood Pressure (Diastolic) History",
                        valueSelector = { it.diastolicBP.toFloat() }
                    )

                    // Body Temperature Graph
                    VitalsGraph(
                        vitalsList = vitalsList,
                        title = "Body Temperature History",
                        valueSelector = { it.temperature }
                    )
                }
            }
            is VitalsHistoryUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No vitals history available")
                }
            }
            is VitalsHistoryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = historyState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
} 