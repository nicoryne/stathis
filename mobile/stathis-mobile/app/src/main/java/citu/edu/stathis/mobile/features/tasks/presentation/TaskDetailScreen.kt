package citu.edu.stathis.mobile.features.tasks.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import coil3.compose.AsyncImage
import citu.edu.stathis.mobile.features.tasks.presentation.components.LessonContent
import citu.edu.stathis.mobile.features.tasks.presentation.components.LessonPage
import citu.edu.stathis.mobile.features.tasks.presentation.components.LessonView
import citu.edu.stathis.mobile.features.tasks.presentation.components.QuizContent
import citu.edu.stathis.mobile.features.tasks.presentation.components.QuizQuestion
import citu.edu.stathis.mobile.features.tasks.presentation.components.QuizView
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val task by viewModel.selectedTask.collectAsState()
    val progress by viewModel.taskProgress.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(taskId) {
        viewModel.loadTaskDetails(taskId)
        viewModel.loadTaskProgress(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = task?.name ?: "Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (error != null) {
                ErrorMessage(
                    message = error!!,
                    onDismiss = viewModel::clearError
                )
            }

            task?.let { currentTask ->
                // Task Image
                if (!currentTask.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = currentTask.imageUrl,
                        contentDescription = "Task image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Task Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = currentTask.name,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentTask.description,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Due Dates
                    DueDateSection(
                        submissionDate = currentTask.submissionDate,
                        closingDate = currentTask.closingDate
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Progress Section
                    progress?.let { currentProgress ->
                        TaskProgressSection(
                            progress = currentProgress,
                            task = currentTask,
                            onLessonComplete = viewModel::completeLesson,
                            onExerciseComplete = viewModel::completeExercise,
                            onQuizSubmit = viewModel::submitQuizScore
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DueDateSection(
    submissionDate: OffsetDateTime,
    closingDate: OffsetDateTime
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

    Column {
        Text(
            text = "Due Dates",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Submission",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = submissionDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column {
                Text(
                    text = "Closing",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = closingDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TaskProgressSection(
    progress: TaskProgressResponse,
    task: Task,
    onLessonComplete: (String, String) -> Unit,
    onExerciseComplete: (String, String) -> Unit,
    onQuizSubmit: (String, String, Int) -> Unit
) {
    Column {
        Text(
            text = "Progress",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = progress.progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Components Section
        if (!task.lessonTemplateId.isNullOrEmpty()) {
            ComponentCard(
                title = "Lesson",
                isCompleted = task.lessonTemplateId in progress.completedLessons,
                onClick = { onLessonComplete(task.physicalId, task.lessonTemplateId) }
            )
            Spacer(Modifier.height(8.dp))
            // Placeholder lesson UI using expected JSON structure
            val demoLesson = LessonContent(
                physicalId = task.lessonTemplateId,
                title = "Lesson",
                description = "Read the pages and mark complete",
                pages = listOf(
                    LessonPage("p1", 1, "Intro", "Welcome to the lesson."),
                    LessonPage("p2", 2, "Concepts", "Core concepts explained.")
                )
            )
            LessonView(lesson = demoLesson)
        }

        if (!task.exerciseTemplateId.isNullOrEmpty()) {
            ComponentCard(
                title = "Exercise",
                isCompleted = task.exerciseTemplateId in progress.completedExercises,
                onClick = { onExerciseComplete(task.physicalId, task.exerciseTemplateId) }
            )
        }

        if (!task.quizTemplateId.isNullOrEmpty()) {
            val quizScore = progress.quizScores[task.quizTemplateId]
            QuizCard(
                score = quizScore,
                maxAttempts = task.maxAttempts,
                onSubmit = { score ->
                    onQuizSubmit(task.physicalId, task.quizTemplateId, score)
                }
            )
            Spacer(Modifier.height(8.dp))
            // Placeholder quiz UI based on expected JSON structure
            val demoQuiz = QuizContent(
                physicalId = task.quizTemplateId,
                title = "Quick Quiz",
                instruction = "Choose the best answer.",
                maxScore = 10,
                questions = listOf(
                    QuizQuestion("q1", 1, "Test 1", listOf("1","2","3"), 0),
                    QuizQuestion("q2", 2, "Test 2", listOf("1","2","3"), 1)
                )
            )
            QuizView(
                quiz = demoQuiz,
                onSubmitScore = { score ->
                    onQuizSubmit(task.physicalId, task.quizTemplateId!!, score)
                }
            )
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
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = onClick,
                enabled = !isCompleted
            ) {
                Text(text = if (isCompleted) "Completed" else "Start")
            }
        }
    }
}

@Composable
private fun QuizCard(
    score: Int?,
    maxAttempts: Int,
    onSubmit: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var quizScore by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quiz",
                style = MaterialTheme.typography.titleMedium
            )

            if (score != null) {
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = { showDialog = true },
                enabled = score == null
            ) {
                Text(text = if (score != null) "Submitted" else "Submit Score")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Submit Quiz Score") },
            text = {
                Column {
                    Text("Enter your quiz score:")
                    Slider(
                        value = quizScore.toFloat(),
                        onValueChange = { quizScore = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 100
                    )
                    Text("Score: $quizScore")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmit(quizScore)
                        showDialog = false
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 