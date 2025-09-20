package citu.edu.stathis.mobile.features.tasks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import citu.edu.stathis.mobile.core.ui.icons.CustomIcons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import citu.edu.stathis.mobile.core.theme.BrandColors
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.presentation.viewmodel.TaskDetailViewModel
import citu.edu.stathis.mobile.features.tasks.presentation.viewmodel.TaskState
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavHostController,
    taskId: String,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val taskState by viewModel.taskState.collectAsState()
    val progressState by viewModel.progressState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Load task data when the screen is first displayed
    LaunchedEffect(key1 = taskId) {
        viewModel.loadTaskDetails(taskId)
        viewModel.loadTaskProgress(taskId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (taskState) {
                            is TaskState.Success -> (taskState as TaskState.Success).task.name
                            else -> "Task Details"
                        },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (taskState) {
                is TaskState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BrandColors.Purple
                    )
                }
                is TaskState.Success -> {
                    val task = (taskState as TaskState.Success).task
                    TaskDetailContent(
                        task = task,
                        progressState = progressState,
                        onStartExercise = {
                            navController.navigate("exercise/${task.exerciseTemplateId}")
                        },
                        onStartLesson = {
                            navController.navigate("lesson/${task.lessonTemplateId}")
                        },
                        onStartQuiz = {
                            navController.navigate("quiz/${task.quizTemplateId}")
                        },
                        onCompleteExercise = { exerciseId ->
                            viewModel.completeExercise(taskId, exerciseId)
                            scope.launch {
                                snackbarHostState.showSnackbar("Exercise completed successfully!")
                            }
                        },
                        onCompleteLesson = { lessonId ->
                            viewModel.completeLesson(taskId, lessonId)
                            scope.launch {
                                snackbarHostState.showSnackbar("Lesson completed successfully!")
                            }
                        },
                        onSubmitQuizScore = { quizId, score ->
                            viewModel.submitQuizScore(taskId, quizId, score)
                            scope.launch {
                                snackbarHostState.showSnackbar("Quiz score submitted successfully!")
                            }
                        }
                    )
                }
                is TaskState.Error -> {
                    ErrorMessage(
                        message = (taskState as TaskState.Error).message,
                        onRetryClick = { viewModel.loadTaskDetails(taskId) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDetailContent(
    task: Task,
    progressState: Any, // Replace with actual progress state type
    onStartExercise: () -> Unit,
    onStartLesson: () -> Unit,
    onStartQuiz: () -> Unit,
    onCompleteExercise: (String) -> Unit,
    onCompleteLesson: (String) -> Unit,
    onSubmitQuizScore: (String, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Task info card
        TaskInfoCard(task = task)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Task content based on type
        when {
            task.exerciseTemplateId != null -> {
                ExerciseTaskContent(
                    task = task,
                    isStarted = task.isStarted,
                    onStartExercise = onStartExercise,
                    onCompleteExercise = { onCompleteExercise(task.exerciseTemplateId) }
                )
            }
            task.lessonTemplateId != null -> {
                LessonTaskContent(
                    task = task,
                    isStarted = task.isStarted,
                    onStartLesson = onStartLesson,
                    onCompleteLesson = { onCompleteLesson(task.lessonTemplateId) }
                )
            }
            task.quizTemplateId != null -> {
                QuizTaskContent(
                    task = task,
                    isStarted = task.isStarted,
                    onStartQuiz = onStartQuiz,
                    onSubmitQuizScore = { score -> onSubmitQuizScore(task.quizTemplateId, score) }
                )
            }
            else -> {
                GenericTaskContent(task = task)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Task progress
        TaskProgressCard(task = task)
    }
}

@Composable
fun TaskInfoCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Task header with icon and status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Task icon based on type
                val (icon, iconTint) = when {
                    task.exerciseTemplateId != null -> CustomIcons.Fitness to BrandColors.Green
                    task.lessonTemplateId != null -> Icons.Default.MenuBook to BrandColors.Teal
                    task.quizTemplateId != null -> Icons.Default.QuestionAnswer to BrandColors.Purple
                    else -> Icons.Default.Assignment to BrandColors.Purple
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = "Task Type",
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Task name
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Task type
                    val taskType = when {
                        task.exerciseTemplateId != null -> "Exercise"
                        task.lessonTemplateId != null -> "Lesson"
                        task.quizTemplateId != null -> "Quiz"
                        else -> "Task"
                    }
                    
                    Text(
                        text = taskType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = iconTint
                    )
                }
                
                // Status indicator
                if (task.isStarted) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Green),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Task description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Task dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Submission date
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Submission Date",
                            tint = BrandColors.Purple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Submission Date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = task.submissionDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Closing date
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Closing Date",
                            tint = BrandColors.Purple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Closing Date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = task.closingDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Attempts info
            if (task.maxAttempts > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Maximum attempts: ${task.maxAttempts}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseTaskContent(
    task: Task,
    isStarted: Boolean,
    onStartExercise: () -> Unit,
    onCompleteExercise: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exercise icon
            Icon(
                imageVector = CustomIcons.Fitness,
                contentDescription = "Exercise",
                tint = BrandColors.Green,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Exercise title
            Text(
                text = "Exercise Task",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Exercise description
            Text(
                text = "Complete this exercise to improve your posture and physical health.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action button
            if (isStarted) {
                Text(
                    text = "You have completed this exercise",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Green
                )
            } else {
                Button(
                    onClick = onStartExercise,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = CustomIcons.Fitness,
                        contentDescription = "Start Exercise"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Start Exercise")
                }
            }
        }
    }
}

@Composable
fun LessonTaskContent(
    task: Task,
    isStarted: Boolean,
    onStartLesson: () -> Unit,
    onCompleteLesson: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lesson icon
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "Lesson",
                tint = BrandColors.Teal,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lesson title
            Text(
                text = "Lesson Task",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Lesson description
            Text(
                text = "Study this lesson to learn important concepts and techniques.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action button
            if (isStarted) {
                Text(
                    text = "You have completed this lesson",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Green
                )
            } else {
                Button(
                    onClick = onStartLesson,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Start Lesson"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Start Lesson")
                }
            }
        }
    }
}

@Composable
fun QuizTaskContent(
    task: Task,
    isStarted: Boolean,
    onStartQuiz: () -> Unit,
    onSubmitQuizScore: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quiz icon
            Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = "Quiz",
                tint = BrandColors.Purple,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quiz title
            Text(
                text = "Quiz Task",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quiz description
            Text(
                text = "Test your knowledge with this quiz to assess your understanding.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action button
            if (isStarted) {
                Text(
                    text = "You have completed this quiz",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Green
                )
            } else {
                Button(
                    onClick = onStartQuiz,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = "Start Quiz"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Start Quiz")
                }
            }
        }
    }
}

@Composable
fun GenericTaskContent(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Task icon
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = "Task",
                tint = BrandColors.Purple,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Task title
            Text(
                text = "General Task",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Task description
            Text(
                text = "This is a general task that requires your attention.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TaskProgressCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Task Progress",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(100.dp)
                )
                
                Text(
                    text = if (task.isStarted) "Completed" else "Not Started",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (task.isStarted) Color.Green else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Completion status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Completion:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(100.dp)
                )
                
                LinearProgressIndicator(
                    progress = if (task.isStarted) 1f else 0f,
                    modifier = Modifier
                        .height(8.dp)
                        .weight(1f),
                    color = BrandColors.Purple
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (task.isStarted) "100%" else "0%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Additional progress information could be added here based on task type
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetryClick,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Retry")
        }
    }
}
