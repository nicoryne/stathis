package citu.edu.stathis.mobile.features.tasks.data.model

data class QuizQuestion(
    val id: String,
    val questionNumber: Int,
    val question: String,
    val options: List<String>,
    val answer: Int
)

data class QuizTemplate(
    val physicalId: String,
    val title: String,
    val instruction: String,
    val maxScore: Int,
    val questions: List<QuizQuestion>
)

