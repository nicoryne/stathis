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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.font.FontWeight
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
                val attempts = LessonAttemptsCache.getAttempts(task.physicalId)
                val isCompleted = attempts > 0
                val canStart = attempts < task.maxAttempts
                LessonCard(
                    isCompleted = isCompleted,
                    canStart = canStart,
                    onStartLesson = { onStartLesson(lesson.physicalId) }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Exercise (open via button similar to quiz/lesson)
            val embeddedExerciseId = task.exerciseTemplateId ?: task.exerciseTemplate?.physicalId
            embeddedExerciseId?.let { templateId ->
                ComponentCard(
                    title = "Exercise",
                    isCompleted = false,
                    onClick = { onStartExercise(templateId) }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Quiz
            val embeddedQuizId = task.quizTemplateId ?: task.quizTemplate?.physicalId
            embeddedQuizId?.let { templateId ->
                QuizCard(
                    score = null,
                    maxScore = task.quizTemplate?.maxScore,
                    maxAttempts = task.maxAttempts,
                    attempts = 0,
                    onTakeQuiz = { onStartQuiz(templateId) }
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
    val quizTemplateState by viewModel.quizTemplate.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(taskId) {
        viewModel.loadTaskDetails(taskId)
        viewModel.loadTaskProgress(taskId)
    }

    // If the task doesn't embed the quiz template but has an ID, fetch it to obtain maxScore from backend
    LaunchedEffect(task?.quizTemplateId, task?.quizTemplate) {
        val templateId = task?.quizTemplateId
        if (templateId != null && task?.quizTemplate == null) {
            viewModel.loadQuizTemplate(templateId)
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
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                           // Quick Stats
                           item {
                               TaskQuickStatsSection(
                                   task = currentTask,
                                   progress = progress,
                                   templateMaxScore = quizTemplateState?.maxScore,
                                   modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                               )
                           }

                    // Due Dates card removed per spec

                       // Progress Section
                       progress?.let { currentProgress ->
                           item {
                               TaskProgressSection(
                                   progress = currentProgress,
                                   task = currentTask,
                                   viewModel = viewModel,
                                   onLessonComplete = viewModel::completeLesson,
                                   onExerciseComplete = viewModel::completeExercise,
                                   onQuizSubmit = viewModel::submitQuizScore,
                                   onStartLesson = { 
                                       if (!isUnavailable) onStartLesson(it) else {
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
                                   onStartExercise = { 
                                       if (!isUnavailable) onStartExercise(it) else {
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
                                   onStartQuiz = { 
                                       if (!isUnavailable) onStartQuiz(it) else {
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
                       } ?: run {
                           // Render components even if progress endpoint 403s, using embedded template if present
                           item {
                               FallbackComponentsSection(
                                   task = currentTask,
                                   viewModel = viewModel,
                                   onStartLesson = { 
                                       if (!isUnavailable) onStartLesson(it) else {
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
                                   onStartExercise = { 
                                       if (!isUnavailable) onStartExercise(it) else {
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
                                   onStartQuiz = { 
                                       if (!isUnavailable) onStartQuiz(it) else {
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
    templateMaxScore: Int?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 8.dp)) {
        val quizId = task.quizTemplateId ?: task.quizTemplate?.physicalId
        if (quizId != null) {
            val score = progress?.quizScore
            val maxScore = (
                task.quizTemplate?.maxScore
                    ?: templateMaxScore
                    ?: progress?.maxQuizScore
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show latest attempt score (backend returns latest in progress.quizScore)
                val latestScore = score
                StatCard(
                    title = "Score",
                    value = if (latestScore != null && maxScore != null && maxScore > 0) "${latestScore}/${maxScore}" else "-",
                    icon = Icons.Default.EmojiEvents,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                val attemptsVal = "${progress?.quizAttempts ?: 0}/${task.maxAttempts}"
                StatCard(
                    title = "Attempts",
                    value = attemptsVal,
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
    val effectiveModifier = if (modifier == Modifier) {
        Modifier
            .width(120.dp)
            .heightIn(min = 120.dp)
    } else modifier
    Card(
        modifier = effectiveModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 1.1
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
private fun TaskProgressSection(
    progress: TaskProgressResponse,
    task: Task,
    viewModel: TaskViewModel,
    onLessonComplete: (String, String) -> Unit,
    onExerciseComplete: (String, String) -> Unit,
    onQuizSubmit: (String, String, Int) -> Unit,
    onStartLesson: (String) -> Unit,
    onStartExercise: (String) -> Unit,
    onStartQuiz: (String) -> Unit,
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
                   Row(
                       verticalAlignment = Alignment.CenterVertically,
                       horizontalArrangement = Arrangement.spacedBy(8.dp)
                   ) {
                       Icon(
                           imageVector = Icons.Default.TrendingUp,
                           contentDescription = "Progress",
                           tint = MaterialTheme.colorScheme.primary,
                           modifier = Modifier.size(20.dp)
                       )
                       Text(
                           text = "Progress",
                           style = MaterialTheme.typography.titleMedium,
                           color = MaterialTheme.colorScheme.primary,
                           fontWeight = FontWeight.Bold
                       )
                   }

                   Spacer(modifier = Modifier.height(16.dp))

                   // Remove overall progress bar from this screen per spec
                   val isQuizOnly = task.lessonTemplateId.isNullOrEmpty() && task.exerciseTemplateId.isNullOrEmpty() && !task.quizTemplateId.isNullOrEmpty()
                   if (false) {
                       Row(
                           modifier = Modifier.fillMaxWidth(),
                           horizontalArrangement = Arrangement.SpaceBetween,
                           verticalAlignment = Alignment.CenterVertically
                       ) {
                           Text(
                               text = "${((progress.progress ?: 0f) * 100).toInt()}% Complete",
                               style = MaterialTheme.typography.bodyMedium,
                               fontWeight = FontWeight.Medium,
                               color = MaterialTheme.colorScheme.onSurface
                           )
                       }

                       Spacer(modifier = Modifier.height(8.dp))

                       LinearProgressIndicator(
                           progress = { progress.progress ?: 0f },
                           modifier = Modifier
                               .fillMaxWidth()
                               .height(8.dp),
                           color = MaterialTheme.colorScheme.primary,
                           trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                       )

                       Spacer(modifier = Modifier.height(20.dp))
                   } else {
                       Spacer(modifier = Modifier.height(8.dp))
                   }

        // Components Section
        if (task.lessonTemplateId?.isNotEmpty() == true || task.lessonTemplate != null) {
            val lessonAttempts = remember(progress, task) {
                if (progress.lessonCompleted == true) LessonAttemptsCache.ensureAtLeast(task.physicalId, 1)
                LessonAttemptsCache.getAttempts(task.physicalId)
            }
            val canStartLesson = lessonAttempts < task.maxAttempts
            val lessonCompleted = (lessonAttempts > 0) || (progress.lessonCompleted == true)
            val lessonTemplatePhysicalId = task.lessonTemplate?.physicalId ?: task.lessonTemplateId
            LessonCard(
                isCompleted = lessonCompleted,
                canStart = canStartLesson,
                onStartLesson = {
                    if (lessonTemplatePhysicalId != null) {
                        onStartLesson(lessonTemplatePhysicalId)
                    } else {
                        // Fallback: navigate with placeholder and let template screen use embedded lesson
                        onStartLesson("embedded")
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            // Attempts stat similar to quiz
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Attempts",
                    value = "${lessonAttempts}/${task.maxAttempts}",
                    icon = Icons.Default.History,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val exerciseTemplatePhysicalId = task.exerciseTemplateId ?: task.exerciseTemplate?.physicalId
        if (!exerciseTemplatePhysicalId.isNullOrEmpty()) {
            ComponentCard(
                title = "Exercise",
                isCompleted = exerciseTemplatePhysicalId in (progress.completedExercises ?: emptyList()),
                onClick = { onStartExercise(exerciseTemplatePhysicalId!!) }
            )
        }

        val quizTemplatePhysicalId = task.quizTemplateId ?: task.quizTemplate?.physicalId
        if (!quizTemplatePhysicalId.isNullOrEmpty()) {
            val quizScore = progress.quizScore
            val maxQuizScore = progress.maxQuizScore
            val attempts = progress.quizAttempts ?: 0
            QuizCard(
                score = quizScore,
                maxScore = maxQuizScore,
                maxAttempts = task.maxAttempts,
                attempts = attempts,
                onTakeQuiz = { onStartQuiz(quizTemplatePhysicalId!!) }
            )
            Spacer(Modifier.height(8.dp))
            // For quiz-only tasks, show a simple completion badge if at least one attempt exists
            val isQuizOnly = task.lessonTemplateId.isNullOrEmpty() && task.exerciseTemplateId.isNullOrEmpty()
            val quizCompletedOnce = attempts > 0
            if (isQuizOnly && quizCompletedOnce) {
                QuizCompletedBanner()
            }
        }
        }
    }
}

@Composable
private fun ComponentCard(
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when (title) {
                        "Lesson" -> Icons.Default.MenuBook
                        "Exercise" -> Icons.Default.FitnessCenter
                        "Quiz" -> Icons.Default.Quiz
                        else -> Icons.Default.Assignment
                    },
                    contentDescription = title,
                    tint = if (isCompleted) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
            Text(
                text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isCompleted) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    if (isCompleted) {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Button(
                onClick = onClick,
                enabled = !isCompleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.primary,
                    contentColor = if (isCompleted) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isCompleted) "Completed" else "Start",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun QuizCompletedBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Quiz Completed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "You can retake until you reach the max attempts.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun QuizCard(
    score: Int?,
    maxScore: Int?,
    maxAttempts: Int,
    attempts: Int = 0,
    onTakeQuiz: () -> Unit
) {
    val attemptsText = "$attempts/$maxAttempts attempts"
    val attemptsExhausted = attempts >= maxAttempts

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
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
                            imageVector = Icons.Default.Quiz,
                            contentDescription = "Quiz",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quiz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = attemptsText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (score != null && maxScore != null && maxScore > 0) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Score $score/$maxScore") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = onTakeQuiz,
                    enabled = maxAttempts > 0 && !attemptsExhausted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (maxAttempts > 0 && !attemptsExhausted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (maxAttempts > 0 && !attemptsExhausted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        when {
                            maxAttempts <= 0 -> "Unavailable"
                            attemptsExhausted -> "Max attempts reached"
                            else -> "Take Quiz"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (maxAttempts > 0 && !attemptsExhausted) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
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
private fun LessonCard(
    isCompleted: Boolean,
    canStart: Boolean,
    onStartLesson: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
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
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Lesson",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lesson",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isCompleted) "Completed" else "Not started",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = onStartLesson,
                    enabled = canStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (canStart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        if (canStart) "Start Lesson" else "Max attempts reached",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (canStart) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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