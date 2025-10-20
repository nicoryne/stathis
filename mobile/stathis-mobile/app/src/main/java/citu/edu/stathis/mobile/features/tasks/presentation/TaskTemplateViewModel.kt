package citu.edu.stathis.mobile.features.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.tasks.data.model.*
import citu.edu.stathis.mobile.features.tasks.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskTemplateViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val streakManager: citu.edu.stathis.mobile.core.streak.StreakManager
) : ViewModel() {

    private val _templateState = MutableStateFlow<TemplateState>(TemplateState.Loading)
    val templateState: StateFlow<TemplateState> = _templateState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _taskDetail = MutableStateFlow<Task?>(null)
    val taskDetail: StateFlow<Task?> = _taskDetail.asStateFlow()

    fun loadTemplate(taskId: String, templateType: String, templateId: String? = null) {
        viewModelScope.launch {
            try {
                _templateState.value = TemplateState.Loading
                _error.value = null

                // Fetch task details for deadline/isActive context (used for auto-submit and guards)
                runCatching {
                    taskRepository.getStudentTask(taskId).first()
                }.onSuccess { task ->
                    _taskDetail.value = task
                }.onFailure { e ->
                    // Non-fatal; proceed without task detail
                    _error.value = _error.value
                }

                val template = when (templateType) {
                    "LESSON" -> {
                        val embedded = _taskDetail.value?.lessonTemplate
                        when {
                            !templateId.isNullOrBlank() && templateId != "embedded" -> {
                                runCatching { taskRepository.getLessonTemplate(templateId).first() }
                                    .getOrElse {
                                        embedded ?: throw it
                                    }
                            }
                            embedded != null -> embedded
                            else -> createMockLessonTemplate()
                        }
                    }
                    "QUIZ" -> {
                        if (!templateId.isNullOrBlank()) {
                            taskRepository.getQuizTemplate(templateId).first()
                        } else {
                            createMockQuizTemplate()
                        }
                    }
                    "EXERCISE" -> {
                        if (!templateId.isNullOrBlank()) {
                            taskRepository.getExerciseTemplate(templateId).first()
                        } else {
                            createMockExerciseTemplate()
                        }
                    }
                    else -> throw IllegalArgumentException("Unknown template type: $templateType")
                }

                _templateState.value = TemplateState.Success(template)
            } catch (e: Exception) {
                _templateState.value = TemplateState.Error(e.message ?: "Failed to load template")
                _error.value = e.message
            }
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            try {
                // If current template is a lesson, call completeLesson with its templateId
                val lessonTemplate = (_templateState.value as? TemplateState.Success)?.template as? LessonTemplate
                if (lessonTemplate != null) {
                    taskRepository.completeLesson(taskId, lessonTemplate.physicalId)
                    // Count lesson attempt for UI availability until max attempts is reached
                    LessonAttemptsCache.increment(taskId)
                    // Refresh progress so UI and lists reflect completion immediately
                    runCatching { taskRepository.getTaskProgress(taskId).first() }
                    // Record streak for daily activity
                    streakManager.recordActivity()
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun submitLesson(taskId: String, lessonTemplateId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("TaskTemplateViewModel", "Submitting lesson completion for task: $taskId, template: $lessonTemplateId")
                
                // Submit lesson completion to backend
                taskRepository.completeLesson(taskId, lessonTemplateId)
                
                // Increment lesson attempts in cache
                LessonAttemptsCache.increment(taskId)
                
                // Mark task as completed for immediate UI feedback
                TaskCompletionCache.markCompleted(taskId)
                
                android.util.Log.d("TaskTemplateViewModel", "Lesson submitted successfully")
            } catch (e: Exception) {
                android.util.Log.e("TaskTemplateViewModel", "Failed to submit lesson", e)
                _error.value = e.message
            }
        }
    }

    fun submitQuiz(taskId: String, submission: QuizSubmission) {
        viewModelScope.launch {
            try {
                val template = (_templateState.value as? TemplateState.Success)?.template as? QuizTemplate
                if (template != null) {
                    // Convert QuizSubmission to QuizAutoCheckRequest (just the answer indices)
                    val answerIndices = submission.answers.map { it.selectedAnswer }
                    val autoCheckRequest = QuizAutoCheckRequest(answers = answerIndices)
                    
                    android.util.Log.d("TaskTemplateViewModel", "Submitting quiz with answers: $answerIndices")
                    android.util.Log.d("TaskTemplateViewModel", "TaskId: $taskId, TemplateId: ${template.physicalId}")
                    
                    val scoreResponse = taskRepository.autoCheckQuiz(taskId, template.physicalId, autoCheckRequest).first()
                    android.util.Log.d("TaskTemplateViewModel", "Quiz submitted successfully, score: ${scoreResponse.score ?: 0}")
                    
                    // Optimistically mark completion for immediate UI feedback
                    TaskCompletionCache.markCompleted(taskId)
                    // Record streak
                    streakManager.recordActivity()
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskTemplateViewModel", "Failed to submit quiz", e)
                _error.value = e.message
            }
        }
    }

    fun submitExercise(taskId: String, performance: ExercisePerformance) {
        viewModelScope.launch {
            try {
                android.util.Log.d("TaskTemplateViewModel", "Submitting exercise completion for task: $taskId, template: ${performance.templateId}")
                
                // Mark exercise as completed for the task
                taskRepository.completeExercise(taskId, performance.templateId)
                
                // Increment exercise attempts in cache (using LessonAttemptsCache for now)
                LessonAttemptsCache.increment(taskId)
                
                // Optimistic completion for UI
                TaskCompletionCache.markCompleted(taskId)
                
                android.util.Log.d("TaskTemplateViewModel", "Exercise submitted successfully")
                // Refresh progress so list reflects completion immediately
                runCatching { taskRepository.getTaskProgress(taskId).first() }
                // Record streak
                streakManager.recordActivity()
            } catch (e: Exception) {
                android.util.Log.e("TaskTemplateViewModel", "Failed to submit exercise", e)
                _error.value = e.message
            }
        }
    }

    fun submitQuizScore(taskId: String, templateId: String, score: Int) {
        viewModelScope.launch {
            try {
                // Kept for compatibility if needed elsewhere
                taskRepository.submitQuizScore(taskId, templateId, score).first()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun createMockLessonTemplate(): LessonTemplate {
        return LessonTemplate(
            physicalId = "lesson_001",
            title = "Introduction to Physical Education",
            description = "Learn the basics of physical education and its importance in daily life.",
            content = LessonContent(
                pages = listOf(
                    LessonPage(
                        id = "page_1",
                        pageNumber = 1,
                        subtitle = "What is Physical Education?",
                        paragraph = listOf(
                            "Physical Education (PE) is an educational discipline that focuses on developing physical fitness, motor skills, and knowledge about physical activity.",
                            "It plays a crucial role in promoting healthy lifestyles and overall well-being."
                        )
                    ),
                    LessonPage(
                        id = "page_2",
                        pageNumber = 2,
                        subtitle = "Benefits of Physical Education",
                        paragraph = listOf(
                            "Regular physical activity helps improve cardiovascular health, strengthen muscles and bones, and enhance mental well-being.",
                            "PE also teaches important life skills such as teamwork, discipline, and goal-setting."
                        )
                    ),
                    LessonPage(
                        id = "page_3",
                        pageNumber = 3,
                        subtitle = "Types of Physical Activities",
                        paragraph = listOf(
                            "Physical activities can be categorized into aerobic exercises, strength training, flexibility exercises, and balance activities.",
                            "Each type offers unique benefits and should be included in a well-rounded fitness program."
                        )
                    )
                )
            )
        )
    }

    private fun createMockQuizTemplate(): QuizTemplate {
        return QuizTemplate(
            physicalId = "quiz_001",
            title = "Physical Education Quiz",
            instruction = "Answer all questions to the best of your ability. You can review your answers before submitting.",
            maxScore = 100,
            content = QuizContent(
                questions = listOf(
                    QuizQuestion(
                        id = "q1",
                        questionNumber = 1,
                        question = "What is the primary goal of Physical Education?",
                        options = listOf(
                            "To win competitions",
                            "To develop physical fitness and motor skills",
                            "To become a professional athlete",
                            "To avoid other subjects"
                        ),
                        answer = 1
                    ),
                    QuizQuestion(
                        id = "q2",
                        questionNumber = 2,
                        question = "Which of the following is NOT a benefit of regular physical activity?",
                        options = listOf(
                            "Improved cardiovascular health",
                            "Stronger muscles and bones",
                            "Increased stress levels",
                            "Better mental well-being"
                        ),
                        answer = 2
                    ),
                    QuizQuestion(
                        id = "q3",
                        questionNumber = 3,
                        question = "How often should children engage in physical activity?",
                        options = listOf(
                            "Once a week",
                            "At least 60 minutes daily",
                            "Only during PE class",
                            "When they feel like it"
                        ),
                        answer = 1
                    )
                )
            )
        )
    }

    private fun createMockExerciseTemplate(): ExerciseTemplate {
        return ExerciseTemplate(
            physicalId = "exercise_001",
            title = "Push-Up Challenge",
            description = "Complete a set of push-ups with proper form to improve upper body strength.",
            exerciseType = "PUSH_UP",
            exerciseDifficulty = "BEGINNER",
            goalReps = 10,
            goalAccuracy = 80,
            goalTime = 60
        )
    }
}
