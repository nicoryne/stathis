package citu.edu.stathis.mobile.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Stathis Design System - Duolingo-inspired design tokens
 * Building on existing Purple (#9334EA) and Teal (#25ACA4) branding
 */

// ===== ENHANCED COLOR PALETTE =====

// Primary Brand Colors (existing)
val StathisPurple = Color(0xFF9334EA)
val StathisTeal = Color(0xFF25ACA4)

// Extended Color Palette for Duolingo-inspired design
object StathisColors {
    // Primary Colors
    val Primary = StathisPurple
    val PrimaryLight = Color(0xFFF0DBFF) // Light purple background
    val PrimaryDark = Color(0xFF6E528A)
    
    // Secondary Colors  
    val Secondary = StathisTeal
    val SecondaryLight = Color(0xFF9DF2EA) // Light teal background
    val SecondaryDark = Color(0xFF197A74)
    
    // Success & Achievement Colors
    val Success = StathisTeal
    val SuccessLight = Color(0xFFE8F5F4)
    val Achievement = Color(0xFF4CAF50) // Green for achievements
    val AchievementLight = Color(0xFFE8F5E8)
    
    // Warning & Caution
    val Warning = Color(0xFFFFA000) // Orange
    val WarningLight = Color(0xFFFFF3E0)
    
    // Error States
    val Error = Color(0xFFF44336) // Red
    val ErrorLight = Color(0xFFFFEBEE)
    
    // Neutral Colors (using existing theme colors)
    val Background = Color(0xFFFFF7FE)
    val Surface = Color(0xFFFFF7FE)
    val SurfaceVariant = Color(0xFFE9DFEB)
    val OnSurface = Color(0xFF1E1A20)
    val OnSurfaceVariant = Color(0xFF4A454E)
    
    // Text Colors
    val TextPrimary = Color(0xFF1E1A20)
    val TextSecondary = Color(0xFF4A454E)
    val TextDisabled = Color(0xFF7C757E)
    
    // Mascot Colors
    val MascotPrimary = StathisPurple
    val MascotSecondary = StathisTeal
    val MascotAccent = Color(0xFFFFD700) // Gold for special mascot moments
}

// ===== ENHANCED TYPOGRAPHY =====

object StathisTypography {
    // Large, bold headlines for main actions (Duolingo style)
    val HeroTitle = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp
    )
    
    val PageTitle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp
    )
    
    val SectionTitle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    
    // Body text hierarchy
    val BodyLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp
    )
    
    val BodyMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp
    )
    
    val BodySmall = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )
    
    // Button text
    val ButtonLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp
    )
    
    val ButtonMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 22.sp
    )
    
    // Caption and labels
    val Caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    )
    
    // Mascot speech bubble text
    val MascotSpeech = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp
    )
}

// ===== SHAPES & CORNERS =====

object StathisShapes {
    // Card shapes
    val CardShape = RoundedCornerShape(12.dp)
    val CardShapeLarge = RoundedCornerShape(16.dp)
    
    // Button shapes
    val ButtonShape = RoundedCornerShape(24.dp) // Pill-shaped buttons
    val ButtonShapeSmall = RoundedCornerShape(12.dp)
    
    // Mascot container shapes
    val MascotContainerShape = RoundedCornerShape(20.dp)
    
    // Progress indicator shapes
    val ProgressShape = RoundedCornerShape(8.dp)
}

// ===== SPACING SYSTEM =====

object StathisSpacing {
    val XS = 4.dp
    val SM = 8.dp
    val MD = 16.dp
    val LG = 24.dp
    val XL = 32.dp
    val XXL = 48.dp
    
    // Component-specific spacing
    val CardPadding = MD
    val ScreenPadding = LG
    val ButtonPadding = MD
    val MascotPadding = LG
}

// ===== ELEVATION SYSTEM =====

object StathisElevation {
    val Card = 2.dp
    val CardHover = 4.dp
    val Button = 1.dp
    val Modal = 8.dp
    val Mascot = 6.dp
}

// ===== ANIMATION DURATIONS =====

object StathisAnimations {
    val Fast = 150L
    val Normal = 300L
    val Slow = 500L
    val MascotReaction = 800L
}

// ===== COMPONENT STYLES =====

object StathisComponentStyles {
    // Button styles
    val PrimaryButton = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
    
    val SecondaryButton = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = StathisColors.Primary
    )
    
    // Card styles
    val CardTitle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = StathisColors.TextPrimary
    )
    
    val CardSubtitle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = StathisColors.TextSecondary
    )
    
    // Progress styles
    val ProgressLabel = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = StathisColors.TextPrimary
    )
    
    val ProgressValue = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = StathisColors.Primary
    )
}

// ===== MASCOT DESIGN TOKENS =====

object StathisMascot {
    // Mascot colors
    val PrimaryColor = StathisColors.MascotPrimary
    val SecondaryColor = StathisColors.MascotSecondary
    val AccentColor = StathisColors.MascotAccent
    
    // Mascot speech bubble
    val SpeechBubbleBackground = StathisColors.PrimaryLight
    val SpeechBubbleText = StathisColors.TextPrimary
    
    // Mascot states
    val HappyColor = StathisColors.Achievement
    val EncouragingColor = StathisColors.Secondary
    val CelebratingColor = StathisColors.MascotAccent
    val ConcernedColor = StathisColors.Warning
}

// ===== GAMIFICATION COLORS =====

object StathisGamification {
    // Streak colors
    val StreakActive = StathisColors.Achievement
    val StreakInactive = StathisColors.TextDisabled
    
    // Level colors
    val LevelBeginner = StathisColors.Secondary
    val LevelIntermediate = StathisColors.Primary
    val LevelAdvanced = StathisColors.MascotAccent
    
    // Achievement colors
    val AchievementGold = Color(0xFFFFD700)
    val AchievementSilver = Color(0xFFC0C0C0)
    val AchievementBronze = Color(0xFFCD7F32)
    
    // Progress colors
    val ProgressComplete = StathisColors.Achievement
    val ProgressPartial = StathisColors.Warning
    val ProgressEmpty = StathisColors.SurfaceVariant
}


