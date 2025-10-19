package citu.edu.stathis.mobile.features.tasks.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.data.model.LessonTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizTemplate
import citu.edu.stathis.mobile.features.tasks.presentation.components.LessonTemplateRenderer
import citu.edu.stathis.mobile.features.tasks.presentation.components.QuizTemplateRenderer
import citu.edu.stathis.mobile.features.tasks.presentation.components.ExerciseTemplateRenderer
import coil3.compose.AsyncImage

@Composable
private fun FallbackComponentsSection(
    task: Task,
    viewModel: TaskViewModel,
    onStartExercise: (String) -> Unit,
    onStartQuiz: (String) -> Unit,
    onStartLesson: (String) -> Unit,
    onBackAfterLesson: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Lesson (use embedded template if present). Open in dedicated screen.
            task.lessonTemplate?.let { lesson ->
                TaskComponentCard(
                    title = "Lesson",
                    icon = Icons.Default.MenuBook,
                    isCompleted = false,
                    attempts = 0,
                    maxAttempts = task.maxAttempts,
                    canStart = true,
                    onClick = { onStartLesson(lesson.physicalId) }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Exercise (open via button similar to quiz/lesson)
            val embeddedExerciseId = task.exerciseTemplateId ?: task.exerciseTemplate?.physicalId
            embeddedExerciseId?.let { templateId ->
                TaskComponentCard(
                    title = "Exercise",
                    icon = Icons.Default.FitnessCenter,
                    isCompleted = false,
                    attempts = 0,
                    maxAttempts = task.maxAttempts,
                    canStart = true,
                    onClick = { onStartExercise(templateId) }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Quiz
            val embeddedQuizId = task.quizTemplateId ?: task.quizTemplate?.physicalId
            embeddedQuizId?.let { templateId ->
                TaskComponentCard(
                    title = "Quiz",
                    icon = Icons.Default.Quiz,
                    isCompleted = false,
                    attempts = 0,
                    maxAttempts = task.maxAttempts,
                    canStart = true,
                    onClick = { onStartQuiz(templateId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onStartLesson: (String) -> Unit = {},
    onStartQuiz: (String) -> Unit = {},
    onStartExercise: (String) -> Unit = {},
    viewModel: TaskViewModel = hiltViewModel()
) {
    val task by viewModel.selectedTask.collectAsState()
    val progress by viewModel.taskProgress.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(taskId) {
        viewModel.loadTaskDetails(taskId)
        // Use suppressError = true to avoid showing 403 error banners
        viewModel.loadTaskProgressWithSuppressError(taskId)
    }

    // Load scores when task is available
    LaunchedEffect(task) {
        task?.let { currentTask ->
            val quizTemplateId = currentTask.quizTemplateId ?: currentTask.quizTemplate?.physicalId
            if (!quizTemplateId.isNullOrBlank()) {
                viewModel.refreshTaskProgressWithScores(taskId, quizTemplateId)
            }
        }
    }

    // Refresh progress when task completion cache is updated (e.g., after quiz submission)
    val completionUpdates by TaskCompletionCache.completionUpdates.collectAsState()
    LaunchedEffect(completionUpdates) {
        if (completionUpdates > 0) {
            // Task completion cache was updated, refresh progress to get latest data
            val quizTemplateId = task?.quizTemplateId ?: task?.quizTemplate?.physicalId
            if (!quizTemplateId.isNullOrBlank()) {
                viewModel.refreshTaskProgressWithScores(taskId, quizTemplateId)
            } else {
                viewModel.refreshTaskProgress(taskId)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = task?.name ?: "Assignment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
            // Calculate lesson attempts based on completion status (more reliable than cache)
            val lessonAttempts = remember(progress) {
                if (progress?.lessonCompleted == true) 1 else 0
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Error Banner
            if (error != null) {
                    item {
                        TaskErrorMessage(
                    message = error!!,
                            onDismiss = viewModel::clearError,
                            modifier = Modifier.padding(16.dp)
                )
                    }
            }

            task?.let { currentTask ->
                    val pastDeadline = runCatching { OffsetDateTime.parse(currentTask.closingDate) }
                        .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                    val active = currentTask.isActive ?: true
                    val isUnavailable = pastDeadline || !active
                    // Hero Section
                    item {
                        TaskHeroSection(
                            task = currentTask,
                            modifier = Modifier.padding(24.dp)
                        )
                    }

                    // Quick Stats
                    item {
                        TaskQuickStatsSection(
                            task = currentTask,
                            progress = progress,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }

                    // Due Dates card removed per spec

                    // Components Section - Directly show task components without progress wrapper
                    // Lesson Component (only show when progress is available)
                    if (progress != null && (currentTask.lessonTemplateId?.isNotEmpty() == true || currentTask.lessonTemplate != null)) {
                        val effectiveMaxAttempts = if (currentTask.maxAttempts > 0) currentTask.maxAttempts else 10
                        val canStartLesson = lessonAttempts < effectiveMaxAttempts
                        val lessonCompleted = (lessonAttempts > 0) || (progress?.lessonCompleted == true)
                        val lessonTemplatePhysicalId = currentTask.lessonTemplate?.physicalId ?: currentTask.lessonTemplateId
                        
                        item {
                            TaskComponentCard(
                                title = "Lesson",
                                icon = Icons.Default.MenuBook,
                                isCompleted = lessonCompleted,
                                attempts = lessonAttempts,
                                maxAttempts = effectiveMaxAttempts,
                                canStart = canStartLesson,
                                onClick = {
                                    val targetId = lessonTemplatePhysicalId ?: "embedded"
                                    if (!isUnavailable) {
                                        onStartLesson(targetId)
                                    } else {
                                        coroutineScope.launch {
                                            val reason = buildString {
                                                val pastDeadline = runCatching { OffsetDateTime.parse(currentTask.closingDate) }
                                                    .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                                                val activeVal = currentTask.isActive ?: true
                                                if (!activeVal) append("Task is deactivated.")
                                                if (pastDeadline) {
                                                    if (isNotEmpty()) append(" ")
                                                    append("Deadline has passed.")
                                                }
                                            }.ifBlank { "This task is unavailable." }
                                            snackbarHostState.showSnackbar(reason)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Exercise Component (only show when progress is available)
                    val exerciseTemplatePhysicalId = currentTask.exerciseTemplateId ?: currentTask.exerciseTemplate?.physicalId
                    if (progress != null && !exerciseTemplatePhysicalId.isNullOrEmpty()) {
                        val isExerciseCompleted = exerciseTemplatePhysicalId in (progress?.completedExercises ?: emptyList())
                        val exerciseAttempts = if (isExerciseCompleted) 1 else 0
                        val effectiveMaxAttempts = if (currentTask.maxAttempts > 0) currentTask.maxAttempts else 10
                        val canStartExercise = exerciseAttempts < effectiveMaxAttempts
                        
                        item {
                            TaskComponentCard(
                                title = "Exercise",
                                icon = Icons.Default.FitnessCenter,
                                isCompleted = isExerciseCompleted,
                                attempts = exerciseAttempts,
                                maxAttempts = effectiveMaxAttempts,
                                canStart = canStartExercise,
                                onClick = {
                                    if (!isUnavailable) {
                                        onStartExercise(exerciseTemplatePhysicalId!!)
                                    } else {
                                        coroutineScope.launch {
                                            val reason = buildString {
                                                val pastDeadline = runCatching { OffsetDateTime.parse(currentTask.closingDate) }
                                                    .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                                                val activeVal = currentTask.isActive ?: true
                                                if (!activeVal) append("Task is deactivated.")
                                                if (pastDeadline) {
                                                    if (isNotEmpty()) append(" ")
                                                    append("Deadline has passed.")
                                                }
                                            }.ifBlank { "This task is unavailable." }
                                            snackbarHostState.showSnackbar(reason)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Quiz Component (only show when progress is available)
                    val quizTemplatePhysicalId = currentTask.quizTemplateId ?: currentTask.quizTemplate?.physicalId
                    if (progress != null && !quizTemplatePhysicalId.isNullOrEmpty()) {
                        val quizScore = progress?.quizScore
                        val maxQuizScore = progress?.maxQuizScore ?: currentTask.quizTemplate?.maxScore
                        val quizAttempts = progress?.quizAttempts ?: 0
                        val effectiveMaxAttempts = if (currentTask.maxAttempts > 0) currentTask.maxAttempts else 10
                        val isQuizCompleted = quizAttempts > 0
                        val canTakeQuiz = quizAttempts < effectiveMaxAttempts
                        
                        val scoreText = if (quizScore != null && maxQuizScore != null && maxQuizScore > 0) {
                            "Score: ${quizScore}/${maxQuizScore}"
                        } else null
                        
                        item {
                            TaskComponentCard(
                                title = "Quiz",
                                icon = Icons.Default.Quiz,
                                isCompleted = isQuizCompleted,
                                attempts = quizAttempts,
                                maxAttempts = effectiveMaxAttempts,
                                canStart = canTakeQuiz,
                                score = scoreText,
                                onClick = {
                                    if (!isUnavailable) {
                                        onStartQuiz(quizTemplatePhysicalId!!)
                                    } else {
                                        coroutineScope.launch {
                                            val reason = buildString {
                                                val pastDeadline = runCatching { OffsetDateTime.parse(currentTask.closingDate) }
                                                    .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                                                val activeVal = currentTask.isActive ?: true
                                                if (!activeVal) append("Task is deactivated.")
                                                if (pastDeadline) {
                                                    if (isNotEmpty()) append(" ")
                                                    append("Deadline has passed.")
                                                }
                                            }.ifBlank { "This task is unavailable." }
                                            snackbarHostState.showSnackbar(reason)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    // Fallback components when progress is not available
                    if (progress == null) {
                        item {
                            FallbackComponentsSection(
                                task = currentTask,
                                viewModel = viewModel,
                                onStartLesson = { templateId ->
                                    if (!isUnavailable) {
                                        onStartLesson(templateId)
                                    } else {
                                        coroutineScope.launch {
                                            val reason = buildString {
                                                val pastDeadline = runCatching { OffsetDateTime.parse(currentTask.closingDate) }
                                                    .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                                                val activeVal = currentTask.isActive ?: true
                                                if (!activeVal) append("Task is deactivated.")
                                                if (pastDeadline) {
                                                    if (isNotEmpty()) append(" ")
                                                    append("Deadline has passed.")
                                                }
                                            }.ifBlank { "This task is unavailable." }
                                            snackbarHostState.showSnackbar(reason)
                                        }
                                    }
                                },
                                onStartExercise = { templateId ->
                                    if (!isUnavailable) {
                                        onStartExercise(templateId)
                                    } else {
                                        coroutineScope.launch {
                                            val reason = buildString {
                                                val pastDeadline = runCatching { OffsetDateTime.parse(currentTask.closingDate) }
                                                    .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                                                val activeVal = currentTask.isActive ?: true
                                                if (!activeVal) append("Task is deactivated.")
                                                if (pastDeadline) {
                                                    if (isNotEmpty()) append(" ")
                                                    append("Deadline has passed.")
                                                }
                                            }.ifBlank { "This task is unavailable." }
                                            snackbarHostState.showSnackbar(reason)
                                        }
                                    }
                                },
                                onStartQuiz = { templateId ->
                                    if (!isUnavailable) {
                                        onStartQuiz(templateId)
                                    } else {
                                        coroutineScope.launch {
                                            val reason = buildString {
                                                val pastDeadline = runCatching { OffsetDateTime.parse(currentTask.closingDate) }
                                                    .getOrNull()?.isBefore(OffsetDateTime.now()) == true
                                                val activeVal = currentTask.isActive ?: true
                                                if (!activeVal) append("Task is deactivated.")
                                                if (pastDeadline) {
                                                    if (isNotEmpty()) append(" ")
                                                    append("Deadline has passed.")
                                                }
                                            }.ifBlank { "This task is unavailable." }
                                            snackbarHostState.showSnackbar(reason)
                                        }
                                    }
                                },
                                onBackAfterLesson = onNavigateBack,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskHeroSection(
    task: Task,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = tween(1000),
        label = "heroScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Task Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = "Assignment",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Task Name
            Text(
                text = task.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Task Image (if available)
            if (!task.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = task.imageUrl,
                    contentDescription = "Task image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun TaskQuickStatsSection(
    task: Task,
    progress: TaskProgressResponse?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 8.dp)) {
        val quizId = task.quizTemplateId ?: task.quizTemplate?.physicalId
        if (quizId != null) {
            val latestScore = progress?.quizScore
            val maxScore = progress?.maxQuizScore ?: task.quizTemplate?.maxScore
            val attempts = progress?.quizAttempts ?: 0
            val effectiveMaxAttempts = if (task.maxAttempts > 0) task.maxAttempts else 10 // Fallback to 10 if maxAttempts is 0
            android.util.Log.d("TaskDetailScreen", "Task maxAttempts: ${task.maxAttempts}, effectiveMaxAttempts: $effectiveMaxAttempts")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Score",
                    value = if (latestScore != null && maxScore != null && maxScore > 0) "$latestScore/$maxScore" else "-",
                    icon = Icons.Default.EmojiEvents,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Attempts",
                    value = "${attempts}/${effectiveMaxAttempts}",
                    icon = Icons.Default.History,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            DueDateWideCard(task.closingDate)
        } else {
            DueDateWideCard(task.closingDate)
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TaskDueDatesSection(
    task: Task,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Due Dates",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
        Text(
            text = "Due Dates",
            style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
        )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                DateItem(
                    label = "Due Date",
                    date = task.closingDate,
                    icon = Icons.Default.LockClock
                )
            }
        }
    }
}

@Composable
private fun DueDateSection(
    submissionDate: String,
    closingDate: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Due Dates",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Due Dates",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                DateItem(
                    label = "Due Date",
                    date = closingDate,
                    icon = Icons.Default.LockClock
                )
            }
        }
    }
}

@Composable
private fun DateItem(
    label: String,
    date: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val parsed = remember(date) {
        runCatching { OffsetDateTime.parse(date) }.getOrNull()
    }
    val formatted = remember(parsed) {
        parsed?.atZoneSameInstant(ZoneId.systemDefault())?.toLocalDate()?.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            ?: date
    }
    val isOverdue = remember(parsed) {
        parsed?.isBefore(OffsetDateTime.now()) == true
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatted,
            style = MaterialTheme.typography.bodySmall,
            color = if (label == "Closing" && isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}



@Composable
private fun DueDateWideCard(
    closingDate: String
) {
    val parsed = remember(closingDate) {
        runCatching { OffsetDateTime.parse(closingDate) }.getOrNull()
    }
    val formattedDue = remember(parsed) {
        parsed?.atZoneSameInstant(ZoneId.systemDefault())?.toLocalDate()?.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            ?: closingDate
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Gradient header for visual polish
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Due Date",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "Due Date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = formattedDue,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                val now = OffsetDateTime.now()
                val parsed = runCatching { OffsetDateTime.parse(closingDate) }.getOrNull()
                val daysText = parsed?.let {
                    val days = ChronoUnit.DAYS.between(now.toLocalDate(), it.toLocalDate())
                    when {
                        days < 0 -> "Overdue by ${kotlin.math.abs(days)} day${if (kotlin.math.abs(days) == 1L) "" else "s"}"
                        days == 0L -> "Due today"
                        days == 1L -> "Due in 1 day"
                        else -> "Due in $days days"
                    }
                } ?: ""
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TaskComponentCard(
    title: String,
    icon: ImageVector,
    isCompleted: Boolean,
    attempts: Int,
    maxAttempts: Int,
    canStart: Boolean,
    score: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side - Icon and text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon with subtle background
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isCompleted) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Text content
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            score != null -> score
                            isCompleted -> "Completed"
                            else -> "Ready to start"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Right side - Action button and subtle attempt indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subtle attempt count (less emphasized)
                Text(
                    text = "${attempts}/${maxAttempts}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                
                // Action button
                Button(
                    onClick = onClick,
                    enabled = canStart,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canStart) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (canStart) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when {
                            !canStart -> "Max reached"
                            isCompleted -> "Retake"
                            else -> "Start"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskErrorMessage(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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