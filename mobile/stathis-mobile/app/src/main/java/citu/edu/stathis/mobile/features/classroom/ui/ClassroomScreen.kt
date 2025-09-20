package citu.edu.stathis.mobile.features.classroom.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.presentation.ClassroomViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomScreen(
    onNavigateToTasks: (String) -> Unit,
    viewModel: ClassroomViewModel = hiltViewModel()
) {
    val classrooms by viewModel.classrooms.collectAsState()
    val selectedClassroom by viewModel.selectedClassroom.collectAsState()
    val classroomProgress by viewModel.classroomProgress.collectAsState()
    val classroomTasks by viewModel.classroomTasks.collectAsState()
    val error by viewModel.error.collectAsState()
    var showEnrollDialog by remember { mutableStateOf(false) }
    var classroomCode by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadStudentClassrooms()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Classrooms",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { showEnrollDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Enroll in Classroom"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (error != null) {
                ErrorMessage(
                    message = error!!,
                    onDismiss = viewModel::clearError
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classrooms) { classroom ->
                    ClassroomCard(
                        classroom = classroom,
                        onClick = {
                            viewModel.loadClassroomDetails(classroom.physicalId)
                            onNavigateToTasks(classroom.physicalId)
                        }
                    )
                }
            }
        }
    }

    if (showEnrollDialog) {
        AlertDialog(
            onDismissRequest = { showEnrollDialog = false },
            title = { Text("Enroll in Classroom") },
            text = {
                OutlinedTextField(
                    value = classroomCode,
                    onValueChange = { classroomCode = it },
                    label = { Text("Classroom Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.enrollInClassroom(classroomCode)
                        showEnrollDialog = false
                        classroomCode = ""
                    },
                    enabled = classroomCode.isNotBlank()
                ) {
                    Text("Enroll")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnrollDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassroomCard(
    classroom: Classroom,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = classroom.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                if (classroom.isActive) {
                    Badge(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        contentColor = Color(0xFF4CAF50)
                    ) {
                        Text("Active")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = classroom.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ClassroomStat(
                    icon = Icons.Default.Person,
                    label = "Students",
                    value = classroom.studentCount.toString()
                )
                ClassroomStat(
                    icon = Icons.Default.Assignment,
                    label = "Teacher",
                    value = classroom.teacherName
                )
            }
        }
    }
}

@Composable
private fun ClassroomStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = message)
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss"
                )
            }
        }
    }
} 