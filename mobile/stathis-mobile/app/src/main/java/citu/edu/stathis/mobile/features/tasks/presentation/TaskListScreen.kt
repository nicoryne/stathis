package citu.edu.stathis.mobile.features.tasks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import java.time.OffsetDateTime
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    classroomId: String,
    onTaskClick: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: TaskViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val error by viewModel.error.collectAsState()
    var typeFilter by remember { mutableStateOf("ALL") }
    var statusFilter by remember { mutableStateOf("ALL") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Store progress data for each task (nullable because fetch may fail)
    var taskProgressMap by remember { mutableStateOf<Map<String, TaskProgressResponse?>>(emptyMap()) }

    LaunchedEffect(classroomId) {
        viewModel.loadTasksForClassroom(classroomId)
    }
    
    // Fetch progress data for all tasks when tasks change
    LaunchedEffect(tasks) {
        if (tasks.isNotEmpty()) {
            val progressMap = mutableMapOf<String, TaskProgressResponse?>()
            tasks.forEach { task ->
                try {
                    // Add detailed logging for debugging
                    android.util.Log.d("TaskListScreen", "Fetching progress for task: ${task.name} (${task.physicalId})")
                    android.util.Log.d("TaskListScreen", "Task details - Active: ${task.isActive}, Started: ${task.isStarted}, Closing: ${task.closingDate}")
                    
                    val progress = viewModel.getTaskProgress(task.physicalId, suppressError = true)
                    if (progress != null) {
                        android.util.Log.d("TaskListScreen", "Progress fetched for ${task.name}: completed=${progress.isCompleted}, progress=${progress.progress}")
                    } else {
                        android.util.Log.w("TaskListScreen", "No progress data for task ${task.name}")
                    }
                    progressMap[task.physicalId] = progress
                } catch (e: Exception) {
                    // If progress fetch fails, continue without it
                    android.util.Log.e("TaskListScreen", "Failed to fetch progress for task ${task.physicalId}: ${e.message}", e)
                }
            }
            taskProgressMap = progressMap
            android.util.Log.d("TaskListScreen", "Final progress map: $progressMap")
        }
    }

    // Ensure tasks and progress refresh when returning to this screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, classroomId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Reload tasks and progress so completion reflects immediately after actions
                viewModel.loadTasksForClassroom(classroomId)
                // Recompute progress after tasks update completes asynchronously
                coroutineScope.launch {
                    // small delay to allow tasks state to settle
                    kotlinx.coroutines.delay(150)
                    val progressMap = mutableMapOf<String, TaskProgressResponse?>()
                    tasks.forEach { task ->
                        try {
                            val progress = viewModel.getTaskProgress(task.physicalId, suppressError = true)
                            progressMap[task.physicalId] = progress
                        } catch (_: Exception) {}
                    }
                    taskProgressMap = progressMap
                }
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Classroom Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Filters row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = typeFilter == "ALL",
                        onClick = { typeFilter = "ALL" },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = typeFilter == "LESSON",
                        onClick = { typeFilter = "LESSON" },
                        label = { Text("Lesson") }
                    )
                    FilterChip(
                        selected = typeFilter == "QUIZ",
                        onClick = { typeFilter = "QUIZ" },
                        label = { Text("Quiz") }
                    )
                    FilterChip(
                        selected = typeFilter == "EXERCISE",
                        onClick = { typeFilter = "EXERCISE" },
                        label = { Text("Exercise") }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == "ALL",
                        onClick = { statusFilter = "ALL" },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = statusFilter == "COMPLETED",
                        onClick = { statusFilter = "COMPLETED" },
                        label = { Text("Completed") }
                    )
                    FilterChip(
                        selected = statusFilter == "ONGOING",
                        onClick = { statusFilter = "ONGOING" },
                        label = { Text("Ongoing") }
                    )
                    FilterChip(
                        selected = statusFilter == "UNAVAILABLE",
                        onClick = { statusFilter = "UNAVAILABLE" },
                        label = { Text("Unavailable") }
                    )
                }

                // Visibility rule: only show tasks that teacher has started
                val visibleTasks = tasks.filter { it.isStarted == true }
                val filtered = visibleTasks.filter { task ->
                    val pastDeadline = runCatching { OffsetDateTime.parse(task.closingDate) }
                        .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                    val hasLesson = task.lessonTemplateId?.isNotEmpty() == true || task.lessonTemplate != null
                    val hasQuiz = task.quizTemplateId?.isNotEmpty() == true || task.quizTemplate != null
                    val hasExercise = task.exerciseTemplateId?.isNotEmpty() == true || task.exerciseTemplate != null

                    val typeOk = when (typeFilter) {
                        "LESSON" -> hasLesson
                        "QUIZ" -> hasQuiz
                        "EXERCISE" -> hasExercise
                        else -> true
                    }

                    // Status logic: prioritize deactivation over completion
                    val progress = taskProgressMap[task.physicalId]
                    val active = task.isActive ?: true
                    val started = task.isStarted ?: false
                    val isUnavailable = pastDeadline || !active
                    
                    // Debug logging for status determination
                    android.util.Log.d("TaskListScreen", "Status logic for ${task.name}:")
                    android.util.Log.d("TaskListScreen", "  - Past deadline: $pastDeadline")
                    android.util.Log.d("TaskListScreen", "  - Active: $active")
                    android.util.Log.d("TaskListScreen", "  - Started: $started")
                    android.util.Log.d("TaskListScreen", "  - Is unavailable: $isUnavailable")
                    android.util.Log.d("TaskListScreen", "  - Progress data: ${progress?.isCompleted}")
                    android.util.Log.d("TaskListScreen", "  - Cache completion: ${TaskCompletionCache.isCompleted(task.physicalId)}")
                    
                    // Completion rule: completed if student has at least one completed attempt (any component)
                    val lessonAttempts = LessonAttemptsCache.getAttempts(task.physicalId)
                    val hasAnyAttempt = (progress?.quizAttempts ?: 0) > 0 ||
                        (progress?.lessonCompleted == true) || (progress?.exerciseCompleted == true) || (lessonAttempts > 0)
                    val isCompleted = !isUnavailable && hasAnyAttempt
                    val isOngoing = !isUnavailable && !isCompleted && active && started
                    
                    android.util.Log.d("TaskListScreen", "  - Final status - Completed: $isCompleted, Ongoing: $isOngoing, Unavailable: $isUnavailable")

                    val statusOk = when (statusFilter) {
                        "COMPLETED" -> isCompleted
                        "ONGOING" -> isOngoing
                        "UNAVAILABLE" -> isUnavailable
                        else -> true
                    }
                    typeOk && statusOk
                }.sortedWith { task1, task2 ->
                    // First, separate available and unavailable tasks
                    val task1PastDeadline = runCatching { OffsetDateTime.parse(task1.closingDate) }
                        .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                    val task1ActiveVal = task1.isActive ?: true
                    val task1Unavailable = task1PastDeadline || !task1ActiveVal
                    
                    val task2PastDeadline = runCatching { OffsetDateTime.parse(task2.closingDate) }
                        .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                    val task2ActiveVal = task2.isActive ?: true
                    val task2Unavailable = task2PastDeadline || !task2ActiveVal
                    
                    // Unavailable tasks go to bottom
                    if (task1Unavailable && !task2Unavailable) return@sortedWith 1
                    if (!task1Unavailable && task2Unavailable) return@sortedWith -1
                    
                    // Within same availability group, sort by due date (earliest first)
                    val date1 = runCatching { OffsetDateTime.parse(task1.closingDate) }.getOrNull()
                    val date2 = runCatching { OffsetDateTime.parse(task2.closingDate) }.getOrNull()
                    
                    when {
                        date1 == null && date2 == null -> 0
                        date1 == null -> 1
                        date2 == null -> -1
                        else -> date1.compareTo(date2)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                // Error Banner
                if (error != null) {
                    item {
                        TaskErrorMessage(
                            message = error!!,
                            onDismiss = viewModel::clearError
                        )
                    }
                }

                // Tasks Header
                item {
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Tasks List
                if (filtered.isEmpty()) {
                    item {
                        EmptyTasksCard()
                    }
                } else {
                    items(filtered) { task ->
                        TaskCard(
                            task = task,
                            progress = taskProgressMap[task.physicalId],
                            onClick = { onTaskClick(task.physicalId) },
                            onUnavailableAttempt = {
                                coroutineScope.launch {
                                    val reason = buildString {
                                        val pastDeadline = runCatching { OffsetDateTime.parse(task.closingDate) }
                                            .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                                        val activeVal = task.isActive ?: true
                                        if (!activeVal) append("Task is deactivated.")
                                        if (pastDeadline) {
                                            if (isNotEmpty()) append(" ")
                                            append("Deadline has passed.")
                                        }
                                    }.ifBlank { "This task is unavailable." }
                                    snackbarHostState.showSnackbar(reason)
                                }
                            }
                        )
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    progress: TaskProgressResponse?,
    onClick: () -> Unit,
    onUnavailableAttempt: () -> Unit
) {
    val pastDeadline = runCatching { OffsetDateTime.parse(task.closingDate) }
        .getOrNull()?.isBefore(OffsetDateTime.now()) == true
    // Status logic: prioritize deactivation over completion
    val active = task.isActive ?: true
    val isUnavailable = pastDeadline || !active
    val isCompleted = if (isUnavailable) {
        false // Deactivated tasks are never considered completed
    } else {
        // Consider task completed if student has at least one attempt/completion on any component
        (progress?.quizAttempts ?: 0) > 0 || (progress?.lessonCompleted == true) || (progress?.exerciseCompleted == true)
    }
    
    // Debug logging for TaskCard
    android.util.Log.d("TaskCard", "TaskCard for ${task.name}:")
    android.util.Log.d("TaskCard", "  - Past deadline: $pastDeadline")
    android.util.Log.d("TaskCard", "  - Active: $active")
    android.util.Log.d("TaskCard", "  - Is unavailable: $isUnavailable")
    android.util.Log.d("TaskCard", "  - Progress completed: ${progress?.isCompleted}")
    android.util.Log.d("TaskCard", "  - Final completed: $isCompleted")
    // Make deactivated or past-deadline tasks show banner when tapped
    val clickableModifier = Modifier.clickable(onClick = { 
        if (isUnavailable) {
            onUnavailableAttempt()
        } else {
            onClick()
        }
    })
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnavailable) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: type icon
            TaskTypeIcon(task)

            // Middle: title, description, due date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnavailable) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Due Date",
                        tint = when {
                            pastDeadline -> MaterialTheme.colorScheme.error
                            !active -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Due: ${task.closingDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            pastDeadline -> MaterialTheme.colorScheme.error
                            !active -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right: type badge + status
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TaskTypeBadge(task)
                TaskStatusChip(
                    isActive = active,
                    isPastDeadline = pastDeadline,
                    isCompleted = isCompleted
                )
            }
        }
    }
}

@Composable
private fun TaskTypeBadge(task: Task) {
    val (icon, label) = when {
        task.lessonTemplateId?.isNotEmpty() == true || task.lessonTemplate != null -> Icons.Default.MenuBook to "Lesson"
        task.quizTemplateId?.isNotEmpty() == true || task.quizTemplate != null -> Icons.Default.Quiz to "Quiz"
        task.exerciseTemplateId?.isNotEmpty() == true || task.exerciseTemplate != null -> Icons.Default.FitnessCenter to "Exercise"
        else -> Icons.Default.Assignment to "Task"
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    )
}

@Composable
private fun TaskStatusChip(
    isActive: Boolean,
    isPastDeadline: Boolean,
    isCompleted: Boolean
) {
    val (backgroundColor, contentColor, text) = when {
        !isActive || isPastDeadline -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Unavailable"
        )
        isCompleted -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Completed"
        )
        isActive -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Ongoing"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Not Started"
        )
    }

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun TaskTypeIcon(task: Task) {
    val icon = when {
        task.lessonTemplateId?.isNotEmpty() == true || task.lessonTemplate != null -> Icons.Default.MenuBook
        task.quizTemplateId?.isNotEmpty() == true || task.quizTemplate != null -> Icons.Default.Quiz
        task.exerciseTemplateId?.isNotEmpty() == true || task.exerciseTemplate != null -> Icons.Default.FitnessCenter
        else -> Icons.Default.Assignment
    }
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
    }
}

@Composable
private fun EmptyTasksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = "No Tasks",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Tasks Available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "There are no tasks assigned to this classroom yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TaskErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
} 