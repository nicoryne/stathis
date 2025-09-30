package citu.edu.stathis.mobile.features.dashboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cit.edu.stathis.mobile.R

/**
 * Mascot emotional states based on user progress and achievements
 */
enum class MascotEmotion {
    DEFAULT,    // Neutral, encouraging
    HAPPY,      // Good progress
    CELEBRATING, // Achievements unlocked
    ENCOURAGING, // Needs motivation
    CONCERNED   // Health alerts or low progress
}

/**
 * Mascot state data class containing drawable resource and speech text
 */
data class MascotState(
    val emotion: MascotEmotion,
    val drawableRes: Int,
    val greeting: String,
    val speechBubbleText: String? = null
) {
    companion object {
        val Default = MascotState(
            emotion = MascotEmotion.DEFAULT,
            drawableRes = R.drawable.mascot_cheer,
            greeting = "Ready to learn something new today?",
            speechBubbleText = "Let's make today productive! 💪"
        )
        
        val Happy = MascotState(
            emotion = MascotEmotion.HAPPY,
            drawableRes = R.drawable.mascot_celebrate,
            greeting = "You're doing amazing! Keep up the great work!",
            speechBubbleText = "Your progress is inspiring! 🌟"
        )
        
        val Celebrating = MascotState(
            emotion = MascotEmotion.CELEBRATING,
            drawableRes = R.drawable.mascot_celebrate,
            greeting = "Congratulations on your achievements! 🎉",
            speechBubbleText = "You've earned this celebration! 🏆"
        )
        
        val Encouraging = MascotState(
            emotion = MascotEmotion.ENCOURAGING,
            drawableRes = R.drawable.mascot_muscles,
            greeting = "You're making great progress! Let's keep going!",
            speechBubbleText = "Every step counts! You've got this! 💪"
        )
        
        val Concerned = MascotState(
            emotion = MascotEmotion.CONCERNED,
            drawableRes = R.drawable.mascot_teacher,
            greeting = "Let's take a break and focus on your health.",
            speechBubbleText = "Your health comes first! Take care of yourself. ❤️"
        )
    }
}

/**
 * Main mascot display component with speech bubble
 */
@Composable
fun MascotDisplay(
    mascotState: MascotState,
    modifier: Modifier = Modifier,
    showSpeechBubble: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech bubble (if enabled and text available)
        if (showSpeechBubble && mascotState.speechBubbleText != null) {
            MascotSpeechBubble(
                text = mascotState.speechBubbleText,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Mascot image
        MascotImage(
            drawableRes = mascotState.drawableRes,
            size = size,
            emotion = mascotState.emotion
        )
    }
}

/**
 * Mascot image with animation based on emotion
 */
@Composable
private fun MascotImage(
    drawableRes: Int,
    size: androidx.compose.ui.unit.Dp,
    emotion: MascotEmotion,
    modifier: Modifier = Modifier
) {
    val animationScale by animateFloatAsState(
        targetValue = when (emotion) {
            MascotEmotion.CELEBRATING -> 1.1f
            MascotEmotion.HAPPY -> 1.05f
            else -> 1f
        },
        animationSpec = tween(800),
        label = "mascot_scale"
    )

    Card(
        modifier = modifier.size(size),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = drawableRes),
                contentDescription = "Mascot",
                modifier = Modifier
                    .size((size * 0.75f))
                    .scale(animationScale),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Compact mascot display for smaller spaces
 */
@Composable
fun CompactMascotDisplay(
    mascotState: MascotState,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 60.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MascotImage(
            drawableRes = mascotState.drawableRes,
            size = size,
            emotion = mascotState.emotion
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = mascotState.greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

/**
 * Mascot with progress indicator
 */
@Composable
fun MascotWithProgress(
    mascotState: MascotState,
    progress: Float,
    progressText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MascotImage(
            drawableRes = mascotState.drawableRes,
            size = 100.dp,
            emotion = mascotState.emotion
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Progress indicator
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }
        }
    }
}

/**
 * Mascot celebration animation for achievements
 */
@Composable
fun MascotCelebration(
    achievementTitle: String,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {}
) {
    var showCelebration by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000) // Show celebration for 3 seconds
        showCelebration = false
        onAnimationComplete()
    }
    
    if (showCelebration) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MascotImage(
                    drawableRes = R.drawable.mascot_celebrate,
                    size = 80.dp,
                    emotion = MascotEmotion.CELEBRATING
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "🎉 Achievement Unlocked! 🎉",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = achievementTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Helper function to determine mascot state based on user data
 */
fun determineMascotState(
    progressPercentage: Float,
    hasNewAchievements: Boolean,
    hasHealthAlerts: Boolean,
    streakCount: Int
): MascotState {
    return when {
        hasHealthAlerts -> MascotState.Concerned
        hasNewAchievements -> MascotState.Celebrating
        progressPercentage >= 80f -> MascotState.Happy
        progressPercentage >= 50f -> MascotState.Encouraging
        streakCount >= 7 -> MascotState.Happy
        else -> MascotState.Default
    }
}

/**
 * Mascot state for different times of day
 */
fun getTimeBasedMascotState(): MascotState {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 6..11 -> MascotState(
            emotion = MascotEmotion.DEFAULT,
            drawableRes = R.drawable.mascot_cheer,
            greeting = "Good morning! Ready to start your day?",
            speechBubbleText = "Morning energy! Let's make today great! ☀️"
        )
        in 12..17 -> MascotState(
            emotion = MascotEmotion.DEFAULT,
            drawableRes = R.drawable.mascot_muscles,
            greeting = "Great afternoon! Keep up the momentum!",
            speechBubbleText = "Afternoon power! You're doing great! 💪"
        )
        in 18..21 -> MascotState(
            emotion = MascotEmotion.DEFAULT,
            drawableRes = R.drawable.mascot_cheer,
            greeting = "Evening session! Let's finish strong!",
            speechBubbleText = "Evening focus! Almost there! 🌅"
        )
        else -> MascotState(
            emotion = MascotEmotion.DEFAULT,
            drawableRes = R.drawable.mascot_teacher,
            greeting = "Late night study? Remember to rest!",
            speechBubbleText = "Don't forget to get some rest! 😴"
        )
    }
}
