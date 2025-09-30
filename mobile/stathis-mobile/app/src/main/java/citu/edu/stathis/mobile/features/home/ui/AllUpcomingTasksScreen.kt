package citu.edu.stathis.mobile.features.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.features.dashboard.presentation.viewmodel.ClassroomsState
import citu.edu.stathis.mobile.features.dashboard.presentation.viewmodel.DashboardViewModel
import citu.edu.stathis.mobile.features.dashboard.presentation.viewmodel.TasksState
import citu.edu.stathis.mobile.features.tasks.data.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllUpcomingTasksScreen(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val tasksState by dashboardViewModel.tasksState.collectAsState()
    val classroomsState by dashboardViewModel.classroomsState.collectAsState()

    LaunchedEffect(Unit) {
        // Ensure data is loaded when arriving at this screen directly
        dashboardViewModel.initializeDashboard()
    }

    val classroomNameById: Map<String, String> = when (val s = classroomsState) {
        is ClassroomsState.Success -> s.classrooms.associate { it.physicalId to it.name }
        else -> emptyMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Upcoming Tasks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val s = tasksState) {
            is TasksState.Success -> {
                val now = java.time.OffsetDateTime.now()
                val availableTasks = s.tasks.filter { task ->
                    val pastDeadline = runCatching { java.time.OffsetDateTime.parse(task.closingDate) }
                        .getOrNull()?.isBefore(now) == true
                    val active = task.isActive ?: true
                    !pastDeadline && active // Only include tasks that are not past deadline and are active
                }
                
                if (availableTasks.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No upcoming tasks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "All tasks are completed or unavailable",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val sorted = availableTasks.sortedBy { it.closingDate }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sorted) { task ->
                            TaskItemRow(
                                task = task,
                                classroomName = classroomNameById[task.classroomPhysicalId],
                                onClick = { navController.navigate("task_detail/${task.physicalId}") }
                            )
                        }
                    }
                }
            }
            is TasksState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TasksState.Empty -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No tasks")
                }
            }
            is TasksState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Failed to load tasks: ${s.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// Uses TaskItemRow from LearnScreen for consistent row styling


