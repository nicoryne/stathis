package citu.edu.stathis.mobile.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.dashboard.presentation.viewmodel.DashboardViewModel
import citu.edu.stathis.mobile.features.dashboard.presentation.viewmodel.*
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomViewModel
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.EnrollmentState
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomsState
import citu.edu.stathis.mobile.features.tasks.navigation.navigateToTaskList

@Composable
fun LearnScreen(
    navController: NavHostController,
    viewModel: ClassroomViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val classroomsState by viewModel.classroomsState.collectAsState()
    val enrollmentState by viewModel.enrollmentState.collectAsState()
    val progressState by dashboardViewModel.progressState.collectAsState()
    val tasksState by dashboardViewModel.tasksState.collectAsState()

    LaunchedEffect(Unit) {
        // Load enrolled classrooms for the student
        viewModel.loadStudentClassrooms()
        // Initialize dashboard to fetch progress, tasks, vitals, etc.
        dashboardViewModel.initializeDashboard()
    }

    var enrollDialog by remember { mutableStateOf(false) }
    var classroomCode by remember { mutableStateOf("") }

    // Handle enrollment state changes
    LaunchedEffect(enrollmentState) {
        when (enrollmentState) {
            is EnrollmentState.Success -> {
                enrollDialog = false
                classroomCode = ""
            }
            is EnrollmentState.Error -> {
                // Error is handled in the dialog
            }
            else -> { /* Loading or Idle states */ }
        }
    }

    // Clean, consistent theme
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
    // Streak Header and Join Class
    StreakHeader(
        streak = 7,
        onJoinClassClick = { enrollDialog = true }
    )

    // Stats Row
    LearnStatsRow(
        progressState = progressState,
        tasksState = tasksState
    )

    // Upcoming Tasks
    LearnUpcomingTasksSection(
        tasksState = tasksState,
        onTaskClick = { taskId -> navController.navigate("task_detail/$taskId") },
        onViewAllTasks = { /* TODO: Implement all tasks screen */ }
    )
            
            // Main Content
            when (val currentState = classroomsState) {
                is ClassroomsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                }
            is ClassroomsState.Empty -> {
                EmptyClassroomsState(
                    onEnrollClick = { enrollDialog = true }
                )
            }
                is ClassroomsState.Success -> {
                    ClassroomsHeader(
                        pathTitle = "Your Classrooms",
                        completedSections = when (val s = classroomsState) {
                            is ClassroomsState.Success -> s.classrooms.size
                            else -> 0
                        },
                        totalSections = 5
                    )
                    ClassroomsContent(
                        classrooms = currentState.classrooms,
                        onClassroomClick = { classroom ->
                            navController.navigate("classroom_detail/${classroom.physicalId}")
                        },
                        onViewTasks = { classroom ->
                            navController.navigateToTaskList(classroom.physicalId)
                        }
                    )
                }
                is ClassroomsState.Error -> {
                    ErrorBanner(
                        message = currentState.message,
                        onDismiss = { viewModel.loadStudentClassrooms() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    EmptyClassroomsState(
                        onEnrollClick = { enrollDialog = true }
                    )
                }
            }
        }
        
        // Floating Join Class Button
        FloatingActionButton(
            onClick = { enrollDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = primaryColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Join Class")
        }
    }

    if (enrollDialog) {
        EnrollmentDialog(
            classroomCode = classroomCode,
            onClassroomCodeChange = { classroomCode = it },
            enrollmentState = enrollmentState,
            onEnroll = { 
                if (classroomCode.isNotBlank()) {
                    viewModel.enrollInClassroom(classroomCode.trim())
                }
            },
            onDismiss = { 
                enrollDialog = false
                classroomCode = ""
                viewModel.resetEnrollmentState()
            }
        )
    }
}

@Composable
private fun LearnStatsRow(
    progressState: ProgressState,
    tasksState: TasksState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LearnStatCard(
            title = "Progress",
            value = when (progressState) {
                is ProgressState.Success -> {
                    val pct = ((progressState.progress.completedTasks.toFloat() /
                        (progressState.progress.totalTasks.takeIf { it > 0 } ?: 1)) * 100).toInt()
                    "$pct%"
                }
                else -> "--"
            },
            icon = Icons.Default.TrendingUp,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        LearnStatCard(
            title = "Tasks",
            value = when (tasksState) {
                is TasksState.Success -> tasksState.tasks.size.toString()
                is TasksState.Empty -> "0"
                else -> "--"
            },
            icon = Icons.Default.Assignment,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LearnStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(96.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TopStatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "21:52",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.SignalWifi4Bar,
                contentDescription = "WiFi",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
            Icon(
                Icons.Default.Battery6Bar,
                contentDescription = "Battery",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "56%",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StreakHeader(
    streak: Int,
    onJoinClassClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Streak Counter
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.clickable { /* Handle streak click */ }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = streak.toString(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Join Class Button
        IconButton(onClick = onJoinClassClick) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Join Class",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ClassroomsHeader(
    pathTitle: String,
    completedSections: Int,
    totalSections: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pathTitle,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CLASSROOMS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "$completedSections/$totalSections",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun LearnUpcomingTasksSection(
    tasksState: TasksState,
    onTaskClick: (String) -> Unit,
    onViewAllTasks: () -> Unit
) {
    // Local copy of the UpcomingTasksSection from Practice to avoid cross-file access
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Tasks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onViewAllTasks) {
                Text("View All")
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        when (tasksState) {
            is TasksState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            is TasksState.Empty -> {
                LearnEmptyStateCard(
                    title = "No tasks yet",
                    description = "Your teacher will assign tasks soon!",
                    icon = Icons.Default.Assignment
                )
            }
            is TasksState.Success -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasksState.tasks.take(3).forEach { task ->
                        TaskItemRow(task = task, onClick = onTaskClick)
                    }
                }
            }
            is TasksState.Error -> {
                ErrorBanner(message = tasksState.message) { }
            }
        }
    }
}

@Composable
private fun LearnEmptyStateCard(
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProgressAndTasksHeader(
    progressState: ProgressState,
    tasksState: TasksState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Learning",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "PROGRESS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = when (progressState) {
                    is ProgressState.Success -> {
                        val pct = ((progressState.progress.completedTasks.toFloat() / (progressState.progress.totalTasks.takeIf { it > 0 } ?: 1)) * 100).toInt()
                        "$pct%"
                    }
                    else -> "--"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tasks quick list
        when (tasksState) {
            is TasksState.Success -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasksState.tasks.take(3).forEach { task ->
                        TaskItemRow(task = task)
                    }
                }
            }
            is TasksState.Loading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            is TasksState.Empty -> {
                Text(
                    text = "No tasks yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is TasksState.Error -> {
                Text(
                    text = tasksState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TaskItemRow(task: Task, onClick: (String) -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(task.physicalId) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
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
                    text = task.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            // Template type indicator inferred from available template IDs
            val icon = when {
                task.lessonTemplateId?.isNotEmpty() == true -> Icons.Default.MenuBook
                task.quizTemplateId?.isNotEmpty() == true -> Icons.Default.Quiz
                task.exerciseTemplateId?.isNotEmpty() == true -> Icons.Default.FitnessCenter
                else -> Icons.Default.Assignment
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
private fun ClassroomsContent(
    classrooms: List<Classroom>,
    onClassroomClick: (Classroom) -> Unit,
    onViewTasks: (Classroom) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(classrooms) { classroom ->
            ClassroomCard(
                classroom = classroom,
                isUnlocked = true, // All enrolled classrooms are unlocked
                progress = 0.0f, // TODO: Calculate actual progress
                onClick = { onClassroomClick(classroom) },
                onViewTasks = { onViewTasks(classroom) }
            )
        }
    }
}

@Composable
private fun ClassroomCard(
    classroom: Classroom,
    isUnlocked: Boolean,
    progress: Float,
    onClick: () -> Unit,
    onViewTasks: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = classroom.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = classroom.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Teacher: ${classroom.teacherName}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            // Progress Circle
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyClassroomsState(
    onEnrollClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.School,
            contentDescription = "No classes",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Start Your Learning Journey",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Join your first class to unlock amazing learning content and start building your skills!",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onEnrollClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    text = "Join Your First Class",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun EnrollmentDialog(
    classroomCode: String,
    onClassroomCodeChange: (String) -> Unit,
    enrollmentState: EnrollmentState,
    onEnroll: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = when (enrollmentState) {
                    is EnrollmentState.Enrolling -> "Joining Class..."
                    is EnrollmentState.Success -> "Success!"
                    is EnrollmentState.Error -> "Error"
                    else -> "Join Class"
                }
            )
        },
        text = {
            Column {
                when (enrollmentState) {
                    is EnrollmentState.Enrolling -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Text("Joining classroom...")
                        }
                    }
                    is EnrollmentState.Success -> {
                        Text("Successfully joined ${enrollmentState.classroom.name}!")
                    }
                    is EnrollmentState.Error -> {
                        Text(
                            text = enrollmentState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = classroomCode,
                            onValueChange = onClassroomCodeChange,
                            label = { Text("Class code") },
                            singleLine = true,
                            isError = true
                        )
                    }
                    else -> {
                        OutlinedTextField(
                            value = classroomCode,
                            onValueChange = onClassroomCodeChange,
                            label = { Text("Class code") },
                            singleLine = true,
                            placeholder = { Text("Enter your class code") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (enrollmentState) {
                is EnrollmentState.Enrolling -> {
                    Button(onClick = { /* No action during loading */ }) {
                        Text("Joining...")
                    }
                }
                is EnrollmentState.Success -> {
                    Button(onClick = onDismiss) {
                        Text("Done")
                    }
                }
                is EnrollmentState.Error -> {
                    Button(
                        onClick = onEnroll,
                        enabled = classroomCode.isNotBlank()
                    ) {
                        Text("Try Again")
                    }
                }
                else -> {
                    Button(
                        onClick = onEnroll,
                        enabled = classroomCode.isNotBlank()
                    ) {
                        Text("Join")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel") 
            }
        }
    )
}
