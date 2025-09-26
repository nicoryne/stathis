package citu.edu.stathis.mobile.features.classroom.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.features.tasks.navigation.navigateToTaskList
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomDetailScreen(
    classroomId: String,
    navController: NavController,
    viewModel: ClassroomViewModel = hiltViewModel()
) {
    val classroom by viewModel.selectedClassroom.collectAsState()
    val progress by viewModel.classroomProgress.collectAsState()

    LaunchedEffect(classroomId) { viewModel.loadClassroomDetails(classroomId) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = classroom?.name ?: "Classroom") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = classroom?.description ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            progress?.let { currentProgress ->
                val progressPercent = if (currentProgress.totalTaskCount > 0) {
                    (currentProgress.completedTaskCount.toFloat() / currentProgress.totalTaskCount * 100).toInt()
                } else 0
                Text(
                    text = "Progress: $progressPercent%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(onClick = { navController.navigateToTaskList(classroomId) }) {
                Text("View Tasks & Activities")
            }
        }
    }
}