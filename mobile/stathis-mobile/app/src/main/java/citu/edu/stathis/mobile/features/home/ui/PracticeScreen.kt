package citu.edu.stathis.mobile.features.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cit.edu.stathis.mobile.R
import citu.edu.stathis.mobile.features.dashboard.presentation.viewmodel.DashboardViewModel
import citu.edu.stathis.mobile.features.dashboard.presentation.viewmodel.*
import citu.edu.stathis.mobile.features.dashboard.ui.components.*
import citu.edu.stathis.mobile.features.progress.data.model.Achievement
import citu.edu.stathis.mobile.features.progress.data.model.ProgressActivity
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.profile.ui.ProfileViewModel
import java.time.format.DateTimeFormatter

@Composable
fun PracticeScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val progressState by viewModel.progressState.collectAsState()
    val achievementsState by viewModel.achievementsState.collectAsState()
    val activitiesState by viewModel.activitiesState.collectAsState()
    // Removed student-specific states from Practice (classrooms, tasks)
    // val classroomsState by viewModel.classroomsState.collectAsState()
    // val tasksState by viewModel.tasksState.collectAsState()
    val vitalsState by viewModel.vitalsState.collectAsState()
    val profileState by profileViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeDashboard()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with greeting and mascot
        item {
            val streakManager = androidx.hilt.navigation.compose.hiltViewModel<citu.edu.stathis.mobile.features.home.viewmodel.LearnViewModel>().streakManager
            val streak by streakManager.streak.collectAsState()
            DashboardHeader(
                userName = profileState.profile?.firstName ?: "Student",
                streakCount = (progressState as? ProgressState.Success)?.progress?.streakDays ?: 0,
                mascotState = determineMascotStateFromProgress(progressState, achievementsState)
            )
        }

        // Quick vitals stat for all users
        item {
            VitalsQuickStat(
                vitalsState = vitalsState,
                onVitalsClick = { navController.navigate("vitals") }
            )
        }

        // Inline exercises list (no separate screen)
        item {
            ExercisesInlineList(
                onStartExercise = { exerciseId ->
                    navController.navigate("practice_session/$exerciseId")
                }
            )
        }

        // Achievements are student-related; omit for guests on Practice

        // Removed separate Exercises container; list is shown inline below

        // Recent activities
        if (activitiesState.isNotEmpty()) {
            item {
                RecentActivitiesSection(
                    activities = activitiesState,
                    onActivityClick = { /* Show activity details */ }
                )
            }
        }

        // Remove classrooms overview from Practice; moved to Learn

        // Bottom padding for navigation bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    streakCount: Int,
    mascotState: MascotState
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Top row with settings only
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification bell icon
            IconButton(
                onClick = { /* Show notifications */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Greeting section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Hello, $userName! 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Mascot, streak, and message section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mascot image only (no container)
            Image(
                painter = painterResource(id = mascotState.drawableRes),
                contentDescription = "Mascot",
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Fit
            )
            
            // Streak indicator
            StreakIndicator(streakCount = streakCount)
            
            // Message
            Text(
                text = mascotState.greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StreakIndicator(streakCount: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "$streakCount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Day Streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Composable
private fun VitalsQuickStat(
    vitalsState: VitalsState,
    onVitalsClick: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Heart Rate
        item {
            StatCard(
                title = "Heart Rate",
                value = when (vitalsState) {
                    is VitalsState.Success -> "${vitalsState.heartRate.toInt()} BPM"
                    else -> "--"
                },
                icon = Icons.Default.Favorite,
                color = MaterialTheme.colorScheme.tertiary,
                onClick = onVitalsClick
            )
        }
        // SpO2
        item {
            StatCard(
                title = "SpO2",
                value = when (vitalsState) {
                    is VitalsState.Success -> "${vitalsState.oxygenSaturation.toInt()}%"
                    else -> "--"
                },
                icon = Icons.Default.Bloodtype,
                color = MaterialTheme.colorScheme.primary
            )
        }
        // Temperature
        item {
            StatCard(
                title = "Temp",
                value = when (vitalsState) {
                    is VitalsState.Success -> "${vitalsState.temperature}°C"
                    else -> "--"
                },
                icon = Icons.Default.Thermostat,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .let { if (onClick != null) it.clickable { onClick() } else it },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                tint = color,
                modifier = Modifier.size(24.dp)
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

// Removed ExercisesEntryCard as content is now inline

@Composable
private fun ExercisesInlineList(
    onStartExercise: (String) -> Unit
) {
    val context = LocalContext.current
    val learnPrefs = remember { citu.edu.stathis.mobile.core.learn.LearnPreferences(context) }
    val level by learnPrefs.levelFlow.collectAsState(initial = citu.edu.stathis.mobile.features.onboarding.domain.model.ExperienceLevel.BEGINNER)
    val templates: List<citu.edu.stathis.mobile.features.exercise.data.templates.ExerciseTemplate> = remember(level) {
        citu.edu.stathis.mobile.features.exercise.data.templates.generateTemplatesForLevel(level)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Exercises",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        templates.forEach { t ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartExercise(t.physicalId) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = t.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!t.description.isNullOrBlank()) {
                            Text(
                                text = t.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementsSection(
    achievements: List<Achievement>,
    onAchievementClick: (Achievement) -> Unit
) {
    Column {
        Text(
            text = "Recent Achievements",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements.take(3)) { achievement ->
                AchievementCard(
                    achievement = achievement,
                    onClick = { onAchievementClick(achievement) }
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Achievement",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun UpcomingTasksSection(
    tasksState: TasksState,
    onTaskClick: (String) -> Unit,
    onViewAllTasks: () -> Unit
) {
    Column {
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

        Spacer(modifier = Modifier.height(12.dp))

        when (tasksState) {
            is TasksState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TasksState.Empty -> {
                EmptyStateCard(
                    title = "No tasks yet",
                    description = "Your teacher will assign tasks soon!",
                    icon = Icons.Default.Assignment
                )
            }
            is TasksState.Success -> {
                val now = java.time.OffsetDateTime.now()
                val availableTasks = tasksState.tasks.filter { task ->
                    val pastDeadline = runCatching { java.time.OffsetDateTime.parse(task.closingDate) }
                        .getOrNull()?.isBefore(now) == true
                    val active = task.isActive ?: true
                    !pastDeadline && active // Only include tasks that are not past deadline and are active
                }
                
                if (availableTasks.isEmpty()) {
                    EmptyStateCard(
                        title = "No upcoming tasks",
                        description = "All tasks are completed or unavailable",
                        icon = Icons.Default.CheckCircle
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableTasks.take(3).forEach { task ->
                            TaskCard(
                                task = task,
                                onClick = { onTaskClick(task.physicalId) }
                            )
                        }
                    }
                }
            }
            is TasksState.Error -> {
                ErrorCard(message = tasksState.message)
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = "Task",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentActivitiesSection(
    activities: List<ProgressActivity>,
    onActivityClick: (ProgressActivity) -> Unit
) {
    Column {
        Text(
            text = "Recent Activities",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activities.take(3).forEach { activity ->
                ActivityCard(
                    activity = activity,
                    onClick = { onActivityClick(activity) }
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: ProgressActivity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Activity",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(
                text = activity.timestamp.format(DateTimeFormatter.ofPattern("MMM dd")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ClassroomsOverviewSection(
    classroomsState: ClassroomsState,
    onClassroomClick: (String) -> Unit,
    onViewAllClassrooms: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Classes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onViewAllClassrooms) {
                Text("View All")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (classroomsState) {
            is ClassroomsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ClassroomsState.Empty -> {
                EmptyStateCard(
                    title = "No classes yet",
                    description = "Join your first class to get started!",
                    icon = Icons.Default.School
                )
            }
            is ClassroomsState.Success -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    classroomsState.classrooms.take(2).forEach { classroom ->
                        SimpleClassroomCard(
                            classroom = classroom,
                            onClick = { onClassroomClick(classroom.physicalId) }
                        )
                    }
                }
            }
            is ClassroomsState.Error -> {
                ErrorCard(message = classroomsState.message)
            }
        }
    }
}

@Composable
private fun SimpleClassroomCard(
    classroom: Classroom,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Classroom",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = classroom.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = classroom.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
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
private fun ErrorCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

// Helper function to determine mascot state based on user progress
private fun determineMascotStateFromProgress(
    progressState: ProgressState,
    achievementsState: List<Achievement>,
    streakCount: Int
): citu.edu.stathis.mobile.features.dashboard.ui.components.MascotState {
    val progressPercentage = when (progressState) {
        is ProgressState.Success -> progressState.progress.completedTasks.toFloat() / progressState.progress.totalTasks
        else -> 0f
    }
    
    return citu.edu.stathis.mobile.features.dashboard.ui.components.determineMascotState(
        progressPercentage = progressPercentage,
        hasNewAchievements = achievementsState.isNotEmpty(),
        hasHealthAlerts = false, // TODO: Get from vitals state
        streakCount = (progressState as? ProgressState.Success)?.progress?.streakDays ?: 0
    )
}
