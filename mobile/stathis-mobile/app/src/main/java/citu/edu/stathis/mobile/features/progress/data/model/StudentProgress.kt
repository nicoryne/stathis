package citu.edu.stathis.mobile.features.progress.data.model

import java.time.OffsetDateTime

/**
 * Represents the overall progress of a student across all classrooms and activities
 */
data class StudentProgress(
    val userId: String,
    val totalXp: Int,
    val level: Int,
    val nextLevelXp: Int,
    val completedTasks: Int,
    val totalTasks: Int,
    val completedExercises: Int,
    val exerciseAccuracy: Float, // 0.0 to 1.0
    val streakDays: Int,
    val lastActive: OffsetDateTime,
    val achievements: List<Achievement>,
    val badges: List<Badge>,
    val classroomProgress: List<ClassroomProgressSummary>,
    val recentActivities: List<ProgressActivity>
)

/**
 * Summarizes a student's progress in a specific classroom
 */
data class ClassroomProgressSummary(
    val classroomId: String,
    val classroomName: String,
    val completedTasksCount: Int,
    val totalTasksCount: Int,
    val completionPercentage: Float, // 0.0 to 1.0
    val earnedXp: Int,
    val rank: Int? // Student's rank in this classroom, null if not ranked
)

/**
 * Represents an achievement that can be earned by students
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val xpReward: Int,
    val isUnlocked: Boolean,
    val unlockedAt: OffsetDateTime?,
    val progress: Float, // 0.0 to 1.0, progress towards unlocking
    val requiredValue: Int, // The value needed to unlock (e.g., 10 exercises)
    val currentValue: Int // Current progress value (e.g., 7 exercises completed)
)

/**
 * Represents a badge that can be earned by students
 */
data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val category: BadgeCategory,
    val rarity: BadgeRarity,
    val isUnlocked: Boolean,
    val unlockedAt: OffsetDateTime?
)

/**
 * Represents a recent activity in the student's progress
 */
data class ProgressActivity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: OffsetDateTime,
    val xpEarned: Int,
    val relatedEntityId: String?, // ID of related task, exercise, etc.
    val relatedEntityType: EntityType?
)

enum class AchievementCategory {
    EXERCISE, TASK, LEARNING, HEALTH, ENGAGEMENT, SPECIAL
}

enum class BadgeCategory {
    EXERCISE, TASK, LEARNING, HEALTH, ENGAGEMENT, SPECIAL
}

enum class BadgeRarity {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY;
    
    fun getColor(): String {
        return when (this) {
            COMMON -> "#8D99AE" // Gray
            UNCOMMON -> "#2B9348" // Green
            RARE -> "#3A86FF" // Blue
            EPIC -> "#8338EC" // Purple
            LEGENDARY -> "#FFB703" // Gold
        }
    }
}

enum class ActivityType {
    TASK_COMPLETED, EXERCISE_COMPLETED, ACHIEVEMENT_UNLOCKED, 
    BADGE_EARNED, LEVEL_UP, STREAK_MILESTONE, CLASSROOM_JOINED
}

enum class EntityType {
    TASK, EXERCISE, LESSON, QUIZ, CLASSROOM, ACHIEVEMENT, BADGE
}
