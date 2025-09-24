package citu.edu.stathis.mobile.core.ui.components

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import citu.edu.stathis.mobile.core.theme.StathisColors
import citu.edu.stathis.mobile.core.theme.StathisMascot
import citu.edu.stathis.mobile.core.theme.StathisShapes
import citu.edu.stathis.mobile.core.theme.StathisSpacing
import citu.edu.stathis.mobile.core.theme.StathisTypography

/**
 * Mascot states for different emotions and interactions
 */
enum class MascotState {
    NEUTRAL,
    HAPPY,
    ENCOURAGING,
    CELEBRATING,
    CONCERNED,
    SLEEPING
}

/**
 * Mascot Avatar component with animations and speech bubbles
 * Uses the Stathis purple/teal color scheme with gold accents
 */
@Composable
fun MascotAvatar(
    state: MascotState = MascotState.NEUTRAL,
    speechText: String? = null,
    modifier: Modifier = Modifier,
    size: Int = 80,
    showSpeechBubble: Boolean = true
) {
    val scale by animateFloatAsState(
        targetValue = when (state) {
            MascotState.CELEBRATING -> 1.1f
            MascotState.HAPPY -> 1.05f
            else -> 1.0f
        },
        animationSpec = tween(300),
        label = "mascotScale"
    )

    val backgroundColor by animateFloatAsState(
        targetValue = when (state) {
            MascotState.HAPPY -> 1.0f
            MascotState.CELEBRATING -> 1.0f
            MascotState.ENCOURAGING -> 0.8f
            MascotState.CONCERNED -> 0.6f
            MascotState.SLEEPING -> 0.4f
            else -> 0.7f
        },
        animationSpec = tween(300),
        label = "mascotBackground"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Bubble
        if (speechText != null && showSpeechBubble) {
            Card(
                shape = StathisShapes.MascotContainerShape,
                colors = CardDefaults.cardColors(
                    containerColor = StathisMascot.SpeechBubbleBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.padding(bottom = StathisSpacing.SM)
            ) {
                Text(
                    text = speechText,
                    style = StathisTypography.MascotSpeech,
                    color = StathisMascot.SpeechBubbleText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(StathisSpacing.MD)
                )
            }
        }

        // Mascot Avatar
        Box(
            modifier = Modifier
                .size(size.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    when (state) {
                        MascotState.HAPPY -> StathisMascot.HappyColor
                        MascotState.CELEBRATING -> StathisMascot.CelebratingColor
                        MascotState.ENCOURAGING -> StathisMascot.EncouragingColor
                        MascotState.CONCERNED -> StathisMascot.ConcernedColor
                        MascotState.SLEEPING -> StathisColors.TextDisabled
                        else -> StathisMascot.PrimaryColor
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Mascot face/icon placeholder
            // TODO: Replace with actual mascot image/icon when available
            Text(
                text = when (state) {
                    MascotState.HAPPY -> "😊"
                    MascotState.CELEBRATING -> "🎉"
                    MascotState.ENCOURAGING -> "💪"
                    MascotState.CONCERNED -> "😟"
                    MascotState.SLEEPING -> "😴"
                    else -> "👋"
                },
                fontSize = (size * 0.4).sp
            )
        }
    }
}

/**
 * Mascot with automatic state changes based on user progress
 */
@Composable
fun InteractiveMascot(
    userLevel: Int = 1,
    streakCount: Int = 0,
    hasNewAchievement: Boolean = false,
    modifier: Modifier = Modifier,
    onMascotClick: () -> Unit = {}
) {
    val mascotState = remember(userLevel, streakCount, hasNewAchievement) {
        when {
            hasNewAchievement -> MascotState.CELEBRATING
            streakCount >= 7 -> MascotState.HAPPY
            streakCount >= 3 -> MascotState.ENCOURAGING
            streakCount == 0 -> MascotState.CONCERNED
            else -> MascotState.NEUTRAL
        }
    }

    val speechText = remember(userLevel, streakCount, hasNewAchievement) {
        when {
            hasNewAchievement -> "Amazing! You earned a new achievement! 🏆"
            streakCount >= 7 -> "Incredible streak! You're on fire! 🔥"
            streakCount >= 3 -> "Great job! Keep up the momentum! 💪"
            streakCount == 0 -> "Ready to start your learning journey? Let's go! 🚀"
            else -> "Welcome back! Ready to learn something new? 📚"
        }
    }

    MascotAvatar(
        state = mascotState,
        speechText = speechText,
        modifier = modifier,
        size = 100
    )
}

/**
 * Compact mascot for smaller spaces
 */
@Composable
fun CompactMascot(
    state: MascotState = MascotState.NEUTRAL,
    modifier: Modifier = Modifier
) {
    MascotAvatar(
        state = state,
        speechText = null,
        modifier = modifier,
        size = 40,
        showSpeechBubble = false
    )
}
