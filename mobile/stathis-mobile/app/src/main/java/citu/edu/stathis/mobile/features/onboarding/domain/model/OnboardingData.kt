package citu.edu.stathis.mobile.features.onboarding.domain.model

data class OnboardingData(
    val learningGoal: LearningGoal? = null,
    val experienceLevel: ExperienceLevel? = null,
    val interests: List<Interest> = emptyList(),
    val isCompleted: Boolean = false
)

enum class LearningGoal {
    HEALTH_FITNESS,
    ACADEMIC_SUCCESS,
    PERSONAL_GROWTH,
    FUN_LEARNING
}

enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class Interest {
    EXERCISE,
    ACADEMICS,
    HEALTH_TRACKING,
    GAMIFICATION,
    SOCIAL_LEARNING,
    ALL_ABOVE
}


// Extension functions for display
fun LearningGoal.displayName(): String = when (this) {
    LearningGoal.HEALTH_FITNESS -> "Health & Fitness"
    LearningGoal.ACADEMIC_SUCCESS -> "Academic Success"
    LearningGoal.PERSONAL_GROWTH -> "Personal Growth"
    LearningGoal.FUN_LEARNING -> "Just for Fun"
}

fun ExperienceLevel.displayName(): String = when (this) {
    ExperienceLevel.BEGINNER -> "Beginner"
    ExperienceLevel.INTERMEDIATE -> "Some Experience"
    ExperienceLevel.ADVANCED -> "Experienced"
}

fun Interest.displayName(): String = when (this) {
    Interest.EXERCISE -> "Physical Exercise"
    Interest.ACADEMICS -> "Academic Studies"
    Interest.HEALTH_TRACKING -> "Health Tracking"
    Interest.GAMIFICATION -> "Gamification"
    Interest.SOCIAL_LEARNING -> "Social Learning"
    Interest.ALL_ABOVE -> "All of the Above"
}

