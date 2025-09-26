package citu.edu.stathis.mobile.features.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.presentation.ClassroomViewModel
import citu.edu.stathis.mobile.features.tasks.navigation.navigateToTaskList

@Composable
fun LearnScreen(navController: NavHostController, viewModel: ClassroomViewModel = hiltViewModel()) {
    val classrooms by viewModel.classrooms.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadStudentClassrooms() }

    var enrollDialog by remember { mutableStateOf(false) }
    var classroomCode by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { enrollDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Enroll")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Your Classes",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (error != null) {
                ErrorBanner(message = error!!, onDismiss = viewModel::clearError)
            }

            if (classrooms.isEmpty()) {
                EmptyState(onEnrollClick = { enrollDialog = true })
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(classrooms) { classroom ->
                        ClassroomRow(
                            classroom = classroom,
                            onClick = {
                                navController.navigate("classroom_detail/${classroom.physicalId}")
                            },
                            onViewTasks = {
                                navController.navigateToTaskList(classroom.physicalId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (enrollDialog) {
        AlertDialog(
            onDismissRequest = { enrollDialog = false },
            title = { Text("Enroll in a class") },
            text = {
                OutlinedTextField(
                    value = classroomCode,
                    onValueChange = { classroomCode = it },
                    label = { Text("Class code") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (classroomCode.isNotBlank()) {
                            viewModel.enrollInClassroom(classroomCode)
                            enrollDialog = false
                            classroomCode = ""
                        }
                    }
                ) { Text("Enroll") }
            },
            dismissButton = {
                TextButton(onClick = { enrollDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ClassroomRow(
    classroom: Classroom,
    onClick: () -> Unit,
    onViewTasks: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = classroom.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = classroom.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Teacher: ${classroom.teacherName}",
                    style = MaterialTheme.typography.labelMedium
                )
                TextButton(onClick = onViewTasks) { Text("View tasks") }
            }
        }
    }
}

@Composable
private fun EmptyState(onEnrollClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No classes yet",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Join your first class with a code",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onEnrollClick) { Text("Enroll") }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}


