package citu.edu.stathis.mobile.features.progress.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import citu.edu.stathis.mobile.features.progress.presentation.viewmodel.ProgressViewModel

/**
 * Simplified Progress Screen - Achievements and health summary
 * Features: Streak tracking, level progression, achievements, health status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    navController: NavHostController,
    progressViewModel: ProgressViewModel = hiltViewModel()
) {
    // Collect data from ViewModel (using actual state properties)
    val progressState by progressViewModel.progressState.collectAsStateWithLifecycle()
    val achievements by progressViewModel.achievementsState.collectAsStateWithLifecycle()
    val badges by progressViewModel.badgesState.collectAsStateWithLifecycle()
    
    // Extract data from state (with fallbacks for now)
    val userLevel = 5 // TODO: Extract from progressState when available
    val currentXP = 3500 // TODO: Extract from progressState when available
    val nextLevelXP = 5000 // TODO: Extract from progressState when available
    val streakCount = 7 // TODO: Extract from progressState when available
    val healthStatus = "Good" // TODO: Extract from progressState when available
    val weeklyExerciseMinutes = 45 // TODO: Extract from progressState when available
    
    // Determine mascot state based on progress
    val mascotState = when {
        achievements.isNotEmpty() -> MascotState.CELEBRATING
        streakCount >= 7 -> MascotState.HAPPY
        badges.isNotEmpty() -> MascotState.ENCOURAGING
        else -> MascotState.NEUTRAL
    }
    
    val mascotSpeech = when {
        achievements.isNotEmpty() -> "Great job! You unlocked a new achievement!"
        streakCount >= 7 -> "Incredible streak! You're unstoppable!"
        badges.isNotEmpty() -> "Your health is looking great! Keep it up!"
        else -> "Keep learning and growing every day!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StathisColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(StathisSpacing.MD)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(StathisSpacing.LG)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                            Text(
                    text = "Progress",
                    style = StathisTypography.HeroTitle,
                    color = StathisColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                // Streak Counter
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = StathisColors.SecondaryLight
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
                            tint = StathisColors.Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(StathisSpacing.XS))
                        Text(
                            text = "$streakCount",
                            style = StathisTypography.titleMedium,
                            color = StathisColors.Secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            // Level and Streak Section
            LevelAndStreakSection(
                userLevel = userLevel,
                currentXP = currentXP,
                nextLevelXP = nextLevelXP,
                streakCount = streakCount
            )

            // Achievements Section
            AchievementsSection(achievements = achievements.map { it.title to it.isUnlocked })

            // Health Summary Section
            HealthSummarySection(
                healthStatus = healthStatus,
                weeklyExerciseMinutes = weeklyExerciseMinutes
            )

            Spacer(modifier = Modifier.height(StathisSpacing.XL))
        }
    }
}

@Composable
private fun LevelAndStreakSection(
    userLevel: Int,
    currentXP: Int,
    nextLevelXP: Int,
    streakCount: Int
) {
    Card(
        shape = StathisShapes.CardShapeLarge,
        colors = CardDefaults.cardColors(
            containerColor = StathisColors.PrimaryLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(StathisSpacing.LG),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Level Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(StathisColors.Primary),
                contentAlignment = Alignment.Center
        ) {
            Text(
                    text = userLevel.toString(),
                    style = StathisTypography.HeroTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(StathisSpacing.MD))

            Text(
                text = "Level $userLevel",
                style = StathisTypography.SectionTitle,
                color = StathisColors.Primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Computer Science Student",
                style = StathisTypography.BodyMedium,
                color = StathisColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(StathisSpacing.MD))

            // XP Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currentXP XP",
                    style = StathisTypography.BodyMedium,
                    color = StathisColors.TextSecondary
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "$nextLevelXP XP",
                    style = StathisTypography.BodyMedium,
                    color = StathisColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(StathisSpacing.SM))

            LinearProgressIndicator(
                progress = currentXP.toFloat() / nextLevelXP.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = StathisColors.Primary,
                trackColor = StathisColors.SurfaceVariant
            )

            Spacer(modifier = Modifier.height(StathisSpacing.LG))

            // Streak Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StathisSpacing.SM)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = StathisColors.Achievement,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$streakCount day streak",
                    style = StathisTypography.BodyLarge,
                    color = StathisColors.Achievement,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AchievementsSection(
    achievements: List<Pair<String, Boolean>>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Achievements",
            style = StathisTypography.SectionTitle,
            color = StathisColors.TextPrimary,
            modifier = Modifier.padding(bottom = StathisSpacing.MD)
        )

        achievements.forEach { (achievement, isUnlocked) ->
            Card(
                shape = StathisShapes.CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) {
                        StathisColors.AchievementLight
                    } else {
                        StathisColors.SurfaceVariant
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
            verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                        imageVector = if (isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                        contentDescription = "Achievement",
                        tint = if (isUnlocked) {
                            StathisColors.Achievement
                        } else {
                            StathisColors.TextDisabled
                        },
                    modifier = Modifier.size(24.dp)
                )

                    Spacer(modifier = Modifier.width(StathisSpacing.MD))

                    Text(
                        text = achievement,
                        style = StathisTypography.BodyMedium,
                        color = if (isUnlocked) {
                            StathisColors.TextPrimary
                        } else {
                            StathisColors.TextDisabled
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (isUnlocked) {
                        Text(
                            text = "✓",
                            style = StathisTypography.BodyLarge,
                            color = StathisColors.Achievement,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthSummarySection(
    healthStatus: String,
    weeklyExerciseMinutes: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Health Summary",
            style = StathisTypography.SectionTitle,
            color = StathisColors.TextPrimary,
            modifier = Modifier.padding(bottom = StathisSpacing.MD)
        )

        Card(
            shape = StathisShapes.CardShape,
            colors = CardDefaults.cardColors(
                containerColor = StathisColors.SecondaryLight
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(StathisSpacing.CardPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Health",
                        tint = StathisColors.Secondary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(StathisSpacing.MD))

                    Text(
                        text = "Health Status: $healthStatus",
                        style = StathisTypography.BodyLarge,
                        color = StathisColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(StathisSpacing.MD))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Exercise",
                        tint = StathisColors.Secondary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(StathisSpacing.MD))

        Text(
                        text = "This week: $weeklyExerciseMinutes minutes",
                        style = StathisTypography.BodyMedium,
                        color = StathisColors.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(StathisSpacing.MD))

                // Mascot Health Coach
                MascotAvatar(
                    state = MascotState.ENCOURAGING,
                    speechText = "Great job staying active! Keep it up! 💪",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    size = 60
                )
            }
        }
    }
}