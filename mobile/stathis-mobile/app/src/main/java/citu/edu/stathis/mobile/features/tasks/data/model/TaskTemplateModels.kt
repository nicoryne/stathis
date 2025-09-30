package citu.edu.stathis.mobile.features.tasks.data.model

// Base template types
sealed class TaskTemplateType {
    object LESSON : TaskTemplateType()
    object QUIZ : TaskTemplateType()
    object EXERCISE : TaskTemplateType()
}

// Lesson Template Models
data class LessonPage(
    val id: String,
    val pageNumber: Int,
    val subtitle: String,
    val paragraph: List<String> // Support multiple paragraphs
)

data class LessonContent(
    val pages: List<LessonPage>
)

data class LessonTemplate(
    val physicalId: String,
    val title: String,
    val description: String,
    val content: LessonContent
)

// Quiz Template Models
data class QuizQuestion(
    val id: String,
    val questionNumber: Int,
    val question: String,
    val options: List<String>,
    val answer: Int // Index of correct answer (0-based)
)

data class QuizContent(
    val questions: List<QuizQuestion>
)

data class QuizTemplate(
    val physicalId: String,
    val title: String,
    val instruction: String,
    val maxScore: Int,
    val content: QuizContent
)

// Exercise Template Models
data class ExerciseContent(
    val exerciseType: String, // "PUSH_UP", "SQUATS"
    val exerciseDifficulty: String, // "BEGINNER", "EXPERT"
    val goalReps: Int,
    val goalAccuracy: Int, // Percentage
    val goalTime: Int // Seconds
)

data class ExerciseTemplate(
    val physicalId: String,
    val title: String,
    val description: String,
    val exerciseType: String,
    val exerciseDifficulty: String,
    val goalReps: Int,
    val goalAccuracy: Int,
    val goalTime: Int
)

// Task Template Response (from API)
data class TaskTemplateResponse(
    val physicalId: String,
    val title: String,
    val description: String,
    val templateType: String, // "LESSON", "QUIZ", "EXERCISE"
    val content: Map<String, Any> // Raw content from API
)

// Quiz Submission Models
data class QuizAnswer(
    val questionId: String,
    val selectedAnswer: Int // Index of selected answer
)

data class QuizSubmission(
    val taskId: String,
    val templateId: String,
    val answers: List<QuizAnswer>
)

data class QuizResult(
    val score: Int,
    val maxScore: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val percentage: Float,
    val answers: List<QuizAnswerResult>
)

data class QuizAnswerResult(
    val questionId: String,
    val question: String,
    val selectedAnswer: Int,
    val correctAnswer: Int,
    val isCorrect: Boolean,
    val options: List<String>
)

// Exercise Performance Models
data class ExercisePerformance(
    val taskId: String,
    val templateId: String,
    val actualReps: Int,
    val actualAccuracy: Float,
    val actualTime: Int, // Seconds
    val goalReps: Int,
    val goalAccuracy: Int,
    val goalTime: Int,
    val isCompleted: Boolean,
    val score: Int // Calculated score based on performance
)


