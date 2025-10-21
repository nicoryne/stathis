package citu.edu.stathis.mobile.features.tasks.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.features.tasks.data.model.*
import citu.edu.stathis.mobile.features.tasks.presentation.components.ExerciseTemplateRenderer
import citu.edu.stathis.mobile.features.tasks.presentation.components.LessonTemplateRenderer
import citu.edu.stathis.mobile.features.tasks.presentation.components.QuizTemplateRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTemplateScreen(
    taskId: String,
    templateType: String,
    templateId: String? = null,
    onNavigateBack: () -> Unit = {},
    onTaskCompleted: () -> Unit = {},
    viewModel: TaskTemplateViewModel = hiltViewModel()
) {
    val templateState by viewModel.templateState.collectAsState()
    val error by viewModel.error.collectAsState()
    val taskDetail by viewModel.taskDetail.collectAsState()

    LaunchedEffect(taskId, templateType, templateId) {
        viewModel.loadTemplate(taskId, templateType, templateId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (templateType) {
                            "LESSON" -> "Lesson"
                            "QUIZ" -> "Quiz"
                            "EXERCISE" -> "Exercise"
                            else -> "Task"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = templateState) {
                is TemplateState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is TemplateState.Success -> {
                    when (templateType) {
                        "LESSON" -> {
                            val lessonTemplate = currentState.template as? LessonTemplate
                            if (lessonTemplate != null) {
                                LessonTemplateRenderer(
                                    template = lessonTemplate,
                                    onComplete = {
                                        viewModel.submitLesson(taskId, lessonTemplate.physicalId)
                                        onTaskCompleted()
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                ErrorMessage("Invalid lesson template")
                            }
                        }
                        
                        "QUIZ" -> {
                            val quizTemplate = currentState.template as? QuizTemplate
                            if (quizTemplate != null) {
                                var showScore by remember { mutableStateOf<Int?>(null) }

                                // Auto-submit at deadline if available from task detail
                                val closingIso = remember(taskDetail?.closingDate) { taskDetail?.closingDate }
                                var autoSubmitted by remember { mutableStateOf(false) }
                                LaunchedEffect(closingIso) {
                                    val deadline = closingIso?.let { runCatching { java.time.OffsetDateTime.parse(it) }.getOrNull() }
                                    if (deadline != null) {
                                        val millis = java.time.Duration.between(java.time.OffsetDateTime.now(), deadline).toMillis()
                                        if (millis > 0) {
                                            kotlinx.coroutines.delay(millis)
                                        }
                                        if (!autoSubmitted) {
                                            autoSubmitted = true
                                            val answers = quizTemplate.content.questions.map { q ->
                                                QuizAnswer(
                                                    questionId = q.id,
                                                    selectedAnswer = -1
                                                )
                                            }
                                            val submission = QuizSubmission(
                                                taskId = taskId,
                                                templateId = quizTemplate.physicalId,
                                                answers = answers
                                            )
                                            viewModel.submitQuiz(taskId, submission)
                                            showScore = 0
                                        }
                                    }
                                }

                                QuizTemplateRenderer(
                                    template = quizTemplate,
                                    onSubmit = { submission ->
                                        // Compute local score for immediate feedback
                                        val answers = submission.answers
                                        val correct = quizTemplate.content.questions.count { q ->
                                            val sel = answers.firstOrNull { it.questionId == q.id }?.selectedAnswer
                                            sel == q.answer
                                        }
                                        showScore = correct
                                        // Submit to backend for authoritative auto-check and persistence
                                        val effectiveSubmission = submission.copy(
                                            taskId = taskId,
                                            templateId = quizTemplate.physicalId
                                        )
                                        viewModel.submitQuiz(taskId, effectiveSubmission)
                                    },
                                    onBackToTask = onNavigateBack,
                                    modifier = Modifier.fillMaxSize()
                                )

                                showScore?.let { score ->
                                    AlertDialog(
                                        onDismissRequest = { showScore = null },
                                        title = { Text("Quiz Submitted") },
                                        text = {
                                            Text("You got $score/${quizTemplate.content.questions.size} correct.")
                                        },
                                        confirmButton = {
                                            Button(onClick = {
                                                showScore = null
                                            }) { Text("Done") }
                                        }
                                    )
                                }
                            } else {
                                ErrorMessage("Invalid quiz template")
                            }
                        }
                        
                        "EXERCISE" -> {
                            val exerciseTemplate = currentState.template as? ExerciseTemplate
                            if (exerciseTemplate != null) {
                                // Wait for task detail to load before rendering exercise template
                                val currentTaskDetail = taskDetail
                                if (currentTaskDetail != null) {
                                    ExerciseTemplateRenderer(
                                        template = exerciseTemplate,
                                        classroomId = "${currentTaskDetail.classroomPhysicalId}|${currentTaskDetail.physicalId}", // Encode both classroom and task IDs
                                        onComplete = { performance ->
                                            viewModel.submitExercise(taskId, performance)
                                            onTaskCompleted()
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Show loading while waiting for task detail
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            } else {
                                ErrorMessage("Invalid exercise template")
                            }
                        }
                        
                        else -> {
                            ErrorMessage("Unknown template type: $templateType")
                        }
                    }
                }
                
                is TemplateState.Error -> {
                    ErrorMessage(currentState.message)
                }
                
                is TemplateState.Empty -> {
                    ErrorMessage("No template found")
                }
            }
            
            // Show error if any
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

// Template State
sealed class TemplateState {
    object Loading : TemplateState()
    data class Success(val template: Any) : TemplateState()
    data class Error(val message: String) : TemplateState()
    object Empty : TemplateState()
}
