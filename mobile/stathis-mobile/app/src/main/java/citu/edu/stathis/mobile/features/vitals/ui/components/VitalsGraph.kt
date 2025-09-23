package citu.edu.stathis.mobile.features.vitals.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import com.patrykandpatrick.vico.compose.chart.Chart

@Composable
fun VitalsGraph(
    vitalsList: List<VitalSigns>,
    title: String,
    valueSelector: (VitalSigns) -> Float,
    modifier: Modifier = Modifier
) {
    val chartEntries = remember(vitalsList) {
        vitalsList.mapIndexed { index, vitals ->
            FloatEntry(index.toFloat(), valueSelector(vitals))
        }
    }

    val chartEntryModel = remember(chartEntries) {
        entryModelOf(chartEntries)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Chart(
                chart = lineChart(),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
} 