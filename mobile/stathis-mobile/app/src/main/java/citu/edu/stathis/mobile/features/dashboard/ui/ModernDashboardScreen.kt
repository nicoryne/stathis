package citu.edu.stathis.mobile.features.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import citu.edu.stathis.mobile.core.theme.StathisColors
import citu.edu.stathis.mobile.core.theme.appColors
import citu.edu.stathis.mobile.core.theme.StathisShapes
import citu.edu.stathis.mobile.core.theme.StathisSpacing
import citu.edu.stathis.mobile.core.theme.StathisTypography
import citu.edu.stathis.mobile.core.ui.components.MascotAvatar
import citu.edu.stathis.mobile.core.ui.components.MascotState
import citu.edu.stathis.mobile.features.auth.ui.components.AuthBackground
import citu.edu.stathis.mobile.features.auth.ui.components.BackgroundDecorations
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomViewModel
import citu.edu.stathis.mobile.features.progress.presentation.viewmodel.ProgressViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Modern Learn Hub Screen - Full-screen immersive experience
 * Features: Mascot-centered design, carousel of learning modules, streak counter, personalized greeting
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDashboardScreen(
    navController: NavHostController,
    onClassroomSelected: (String) -> Unit,
    classroomViewModel: ClassroomViewModel = hiltViewModel(),
    progressViewModel: ProgressViewModel = hiltViewModel()
) {
    // Collect data from ViewModels (using actual state properties)
    val progressState by progressViewModel.progressState.collectAsStateWithLifecycle()
    val achievements by progressViewModel.achievementsState.collectAsStateWithLifecycle()

    val classroomsState by classroomViewModel.classroomsState.collectAsStateWithLifecycle()
    val selectedClassroom by classroomViewModel.selectedClassroom.collectAsStateWithLifecycle()
    val tasksState by classroomViewModel.tasksState.collectAsStateWithLifecycle()

    val colors = appColors()

    // Extract data from state (with fallbacks for now)
    val userName = "Alex" // TODO: Extract from user data
    val userLevel = 5 // TODO: Extract from progressState when available
    val streakCount = 7 // TODO: Extract from progressState when available
    val hasNewAchievement = achievements.isNotEmpty() // TODO: Check for new achievements
    val todayTasks = listOf("Complete Algebra Quiz", "Practice Posture Exercise") // TODO: Extract from classroomViewModel
    
    // Determine mascot state based on user progress
    val mascotState = when {
        hasNewAchievement -> MascotState.CELEBRATING
        streakCount >= 7 -> MascotState.HAPPY
        streakCount >= 3 -> MascotState.ENCOURAGING
        else -> MascotState.NEUTRAL
    }
    
    val mascotSpeech = when {
        hasNewAchievement -> "Congratulations! You earned a new achievement!"
        streakCount >= 7 -> "Amazing streak! You're unstoppable! 🔥"
        streakCount >= 3 -> "Great job! Keep the momentum going!"
        else -> "Hello, $userName! Ready to learn today?"
    }

    LaunchedEffect(Unit) {
        classroomViewModel.loadStudentClassrooms()
    }

    LaunchedEffect(classroomsState) {
        val list = (classroomsState as? citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomsState.Success)?.classrooms
        if (!list.isNullOrEmpty() && selectedClassroom == null) {
            classroomViewModel.selectClassroom(list.first())
        }
    }

    AuthBackground {
        BackgroundDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(StathisSpacing.MD)
        ) {
            // Classrooms first
            Text(
                text = "Your Classrooms",
                style = StathisTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = StathisSpacing.SM)
            )

            when (val state = classroomsState) {
                is citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomsState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.brandPurple
                    )
                }
                is citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomsState.Error -> {
                    Text(
                        text = state.message,
                        style = StathisTypography.BodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomsState.Empty -> {
                    Text(
                        text = "No classrooms yet",
                        style = StathisTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                is citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomsState.Success -> {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(StathisSpacing.MD),
                        contentPadding = PaddingValues(horizontal = StathisSpacing.XS)
                    ) {
                        items(state.classrooms) { classroom ->
                            LearningModuleCard(
                                title = classroom.name,
                                progress = 0,
                                onTap = {
                                    classroomViewModel.selectClassroom(classroom)
                                    onClassroomSelected(classroom.physicalId)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(StathisSpacing.LG))

            // Pending tasks per selected classroom
            Text(
                text = "Pending Tasks",
                style = StathisTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = StathisSpacing.SM)
            )

            when (val tState = tasksState) {
                is citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.TasksState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.brandTeal
                    )
                }
                is citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.TasksState.Error -> {
                    Text(
                        text = tState.message,
                        style = StathisTypography.BodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.TasksState.Empty -> {
                    Text(
                        text = "You're all caught up",
                        style = StathisTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                is citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.TasksState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(StathisSpacing.SM)
                    ) {
                        items(tState.tasks) { task ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = StathisColors.Surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(StathisSpacing.MD),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = task.name,
                                            style = StathisTypography.BodyMedium,
                                            color = StathisColors.TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Due: ${'$'}{task.closingDate}",
                                            style = StathisTypography.BodySmall,
                                            color = StathisColors.TextSecondary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = null,
                                        tint = StathisColors.Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(StathisSpacing.XL))

            // Top Section with Greeting and Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Personalized Greeting
                Column {
                    Text(
                        text = "Hello, $userName! 👋",
                        style = StathisTypography.HeroTitle,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                        style = StathisTypography.BodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                // Streak Counter (Upper Right)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.brandTeal.copy(alpha = 0.15f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(StathisSpacing.SM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = colors.brandTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(StathisSpacing.XS))
                        Text(
                            text = "$streakCount",
                            style = StathisTypography.SectionTitle,
                            color = colors.brandTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(StathisSpacing.XL))

            // Mascot Centered Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mascot with Speech Bubble
                MascotAvatar(
                    state = mascotState,
                    size = 120,
                    speechText = mascotSpeech,
                    showSpeechBubble = true
                )

                Spacer(modifier = Modifier.height(StathisSpacing.LG))

                // Level Progress
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.brandPurple.copy(alpha = 0.12f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(StathisSpacing.MD),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.brandPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$userLevel",
                                style = StathisTypography.HeroTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(StathisSpacing.SM))
                        
                        Column {
                            Text(
                                text = "Level $userLevel",
                                style = StathisTypography.BodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Keep learning to level up!",
                                style = StathisTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(StathisSpacing.XL))


            Spacer(modifier = Modifier.height(StathisSpacing.LG))

            // Quick Actions
            Column {
                Text(
                    text = "Quick Actions",
                    style = StathisTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = StathisSpacing.MD)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(StathisSpacing.MD)
                ) {
                    // Exercise Button
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.brandTeal.copy(alpha = 0.15f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(StathisSpacing.MD),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = "Exercise",
                                tint = colors.brandTeal,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(StathisSpacing.SM))
                            Text(
                                text = "Exercise",
                                style = StathisTypography.BodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Tasks Button
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.brandPurple.copy(alpha = 0.12f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(StathisSpacing.MD),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = "Tasks",
                                tint = colors.brandPurple,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(StathisSpacing.SM))
                            Text(
                                text = "Tasks",
                                style = StathisTypography.BodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningModuleCard(
    title: String,
    progress: Int,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = StathisColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onTap
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(StathisSpacing.MD),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = StathisTypography.BodyMedium,
                    color = StathisColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$progress% Complete",
                    style = StathisTypography.BodySmall,
                    color = StathisColors.TextSecondary
                )
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = StathisColors.Primary,
                trackColor = StathisColors.SurfaceVariant
            )
        }
    }
}
