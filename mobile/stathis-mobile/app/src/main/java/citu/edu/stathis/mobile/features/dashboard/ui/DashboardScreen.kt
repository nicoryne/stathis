package citu.edu.stathis.mobile.features.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import citu.edu.stathis.mobile.core.theme.StathisColors
import citu.edu.stathis.mobile.core.theme.StathisShapes
import citu.edu.stathis.mobile.core.theme.StathisSpacing
import citu.edu.stathis.mobile.core.theme.StathisTypography
import citu.edu.stathis.mobile.core.ui.components.MascotAvatar
import citu.edu.stathis.mobile.core.ui.components.MascotState
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomViewModel
import citu.edu.stathis.mobile.features.progress.presentation.viewmodel.ProgressViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Learn Hub Screen - Main screen inspired by Duolingo
 * Features: Mascot greeting, today's focus, classroom selection, quick actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    onClassroomSelected: (String) -> Unit,
    classroomViewModel: ClassroomViewModel = hiltViewModel(),
    progressViewModel: ProgressViewModel = hiltViewModel()
) {
    // Collect data from ViewModels (using actual state properties)
    val progressState by progressViewModel.progressState.collectAsStateWithLifecycle()
    val achievements by progressViewModel.achievementsState.collectAsStateWithLifecycle()
    
    // Extract data from state (with fallbacks for now)
    val userLevel = 5 // TODO: Extract from progressState when available
    val streakCount = 3 // TODO: Extract from progressState when available
    val hasNewAchievement = achievements.isNotEmpty() // TODO: Check for new achievements
    val selectedClassroom = "Math Class" // TODO: Extract from classroomViewModel
    val todayTasks = listOf("Complete Algebra Quiz", "Practice Posture Exercise") // TODO: Extract from classroomViewModel
    val availableClassrooms = listOf("Math Class", "Science Class", "History Class") // TODO: Extract from classroomViewModel
    
    // Determine mascot state based on user progress
    val mascotState = when {
        hasNewAchievement -> MascotState.CELEBRATING
        streakCount >= 7 -> MascotState.HAPPY
        streakCount >= 3 -> MascotState.ENCOURAGING
        else -> MascotState.NEUTRAL
    }
    
    val mascotSpeech = when {
        hasNewAchievement -> "Congratulations! You earned a new achievement!"
        streakCount >= 7 -> "Amazing streak! You're on fire! 🔥"
        streakCount >= 3 -> "Great job! Keep the momentum going!"
        else -> "Welcome back! Ready to learn today?"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good morning!",
                            style = StathisTypography.PageTitle,
                            color = StathisColors.TextPrimary
                        )
                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                            style = StathisTypography.BodySmall,
                            color = StathisColors.TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Show notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = StathisColors.TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StathisColors.Surface,
                    titleContentColor = StathisColors.TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(StathisSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(StathisSpacing.LG)
        ) {
            // Mascot Greeting Section
            MascotGreetingSection(
                userLevel = userLevel,
                streakCount = streakCount,
                hasNewAchievement = hasNewAchievement
            )

            // Today's Focus Section
            TodaysFocusSection(
                tasks = todayTasks,
                onStartTask = { /* TODO: Navigate to task */ }
            )

            // Classroom Selection Section
            ClassroomSelectionSection(
                classrooms = availableClassrooms,
                selectedClassroom = selectedClassroom,
                onClassroomSelected = onClassroomSelected
            )

            // Quick Actions Section
            QuickActionsSection(
                onExerciseClick = { navController.navigate("exercise") },
                onTasksClick = { navController.navigate("tasks") }
            )

            Spacer(modifier = Modifier.height(StathisSpacing.XL))
        }
    }
}

@Composable
private fun MascotGreetingSection(
    userLevel: Int,
    streakCount: Int,
    hasNewAchievement: Boolean
) {
    Card(
        shape = StathisShapes.CardShapeLarge,
        colors = CardDefaults.cardColors(
            containerColor = StathisColors.PrimaryLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(StathisSpacing.LG),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mascot
            MascotAvatar(
                state = MascotState.NEUTRAL,
                size = 80,
                speechText = "Welcome back! Ready to learn today?",
                showSpeechBubble = true,
                modifier = Modifier.weight(1f)
            )

            // Level and Streak Info
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Level $userLevel",
                    style = StathisTypography.SectionTitle,
                    color = StathisColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$streakCount day streak",
                    style = StathisTypography.BodyMedium,
                    color = StathisColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun TodaysFocusSection(
    tasks: List<String>,
    onStartTask: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Today's Focus",
            style = StathisTypography.SectionTitle,
            color = StathisColors.TextPrimary,
            modifier = Modifier.padding(bottom = StathisSpacing.MD)
        )

        Card(
            shape = StathisShapes.CardShape,
            colors = CardDefaults.cardColors(
                containerColor = StathisColors.Surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(StathisSpacing.CardPadding)
            ) {
                Text(
                    text = tasks.firstOrNull() ?: "No tasks for today",
                    style = StathisTypography.BodyLarge,
                    color = StathisColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                if (tasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(StathisSpacing.MD))
                    
                    Button(
                        onClick = onStartTask,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StathisColors.Primary,
                            contentColor = Color.White
                        ),
                        shape = StathisShapes.ButtonShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Start Learning",
                            style = StathisTypography.ButtonLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassroomSelectionSection(
    classrooms: List<String>,
    selectedClassroom: String,
    onClassroomSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Your Classrooms",
            style = StathisTypography.SectionTitle,
            color = StathisColors.TextPrimary,
            modifier = Modifier.padding(bottom = StathisSpacing.MD)
        )

        classrooms.forEach { classroom ->
            Card(
                shape = StathisShapes.CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (classroom == selectedClassroom) {
                        StathisColors.SecondaryLight
                    } else {
                        StathisColors.Surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = StathisSpacing.XS)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(StathisSpacing.CardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = classroom,
                        style = StathisTypography.BodyMedium,
                        color = StathisColors.TextPrimary
                    )
                    
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Classroom",
                        tint = if (classroom == selectedClassroom) {
                            StathisColors.Secondary
                        } else {
                            StathisColors.TextSecondary
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onExerciseClick: () -> Unit,
    onTasksClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Quick Actions",
            style = StathisTypography.SectionTitle,
            color = StathisColors.TextPrimary,
            modifier = Modifier.padding(bottom = StathisSpacing.MD)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(StathisSpacing.MD)
        ) {
            // Exercise Button
            Card(
                shape = StathisShapes.CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = StathisColors.SecondaryLight
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(StathisSpacing.CardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Exercise",
                        tint = StathisColors.Secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(StathisSpacing.SM))
                    Text(
                        text = "Exercise",
                        style = StathisTypography.BodyMedium,
                        color = StathisColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Tasks Button
            Card(
                shape = StathisShapes.CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = StathisColors.PrimaryLight
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(StathisSpacing.CardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Tasks",
                        tint = StathisColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(StathisSpacing.SM))
                    Text(
                        text = "Tasks",
                        style = StathisTypography.BodyMedium,
                        color = StathisColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}