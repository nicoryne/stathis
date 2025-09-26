package citu.edu.stathis.mobile.features.exercise.data.templates

import citu.edu.stathis.mobile.features.onboarding.domain.model.ExperienceLevel

data class ExerciseTemplate(
    val physicalId: String,
    val title: String,
    val description: String,
    val exerciseType: String,
    val exerciseDifficulty: ExperienceLevel,
    val goalReps: Int?,
    val goalTime: Int?,
    val goalAccuracy: Int?
)

fun generateTemplatesForLevel(level: ExperienceLevel): List<ExerciseTemplate> {
    return when (level) {
        ExperienceLevel.BEGINNER -> listOf(
            ExerciseTemplate(
                physicalId = "EXERCISE-25-6994-096",
                title = "Bodyweight Squats",
                description = "Learn proper squat form with bodyweight reps.",
                exerciseType = "SQUATS",
                exerciseDifficulty = ExperienceLevel.BEGINNER,
                goalReps = 10,
                goalTime = 120,
                goalAccuracy = 90
            ),
            ExerciseTemplate(
                physicalId = "EXERCISE-BEG-002",
                title = "Wall Push-ups",
                description = "Build pushing strength with safe wall push-ups.",
                exerciseType = "PUSH_UPS",
                exerciseDifficulty = ExperienceLevel.BEGINNER,
                goalReps = 8,
                goalTime = 90,
                goalAccuracy = 85
            )
        )
        ExperienceLevel.INTERMEDIATE -> listOf(
            ExerciseTemplate(
                physicalId = "EXERCISE-INT-001",
                title = "Alternating Lunges",
                description = "Improve balance and strength with lunges.",
                exerciseType = "LUNGES",
                exerciseDifficulty = ExperienceLevel.INTERMEDIATE,
                goalReps = 12,
                goalTime = 120,
                goalAccuracy = 90
            ),
            ExerciseTemplate(
                physicalId = "EXERCISE-INT-002",
                title = "Knee Push-ups",
                description = "Scaled push-ups focusing on controlled reps.",
                exerciseType = "PUSH_UPS",
                exerciseDifficulty = ExperienceLevel.INTERMEDIATE,
                goalReps = 10,
                goalTime = 120,
                goalAccuracy = 90
            )
        )
        ExperienceLevel.ADVANCED -> listOf(
            ExerciseTemplate(
                physicalId = "EXERCISE-ADV-001",
                title = "Full Push-ups",
                description = "Standard push-ups emphasizing full ROM.",
                exerciseType = "PUSH_UPS",
                exerciseDifficulty = ExperienceLevel.ADVANCED,
                goalReps = 15,
                goalTime = 120,
                goalAccuracy = 92
            ),
            ExerciseTemplate(
                physicalId = "EXERCISE-ADV-002",
                title = "Jump Squats",
                description = "Power variation to challenge explosiveness.",
                exerciseType = "SQUATS",
                exerciseDifficulty = ExperienceLevel.ADVANCED,
                goalReps = 12,
                goalTime = 90,
                goalAccuracy = 92
            )
        )
    }
}


