package citu.edu.stathis.mobile.features.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.data.model.LessonTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizTemplate
import citu.edu.stathis.mobile.features.tasks.domain.usecase.*
import citu.edu.stathis.mobile.features.common.domain.Result
import citu.edu.stathis.mobile.features.common.domain.asResult
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTasksForClassroomResultUseCase: GetTasksForClassroomResultUseCase,
    private val getTaskDetailsResultUseCase: GetTaskDetailsResultUseCase,
    private val getTaskProgressResultUseCase: GetTaskProgressResultUseCase,
    private val getLessonTemplateResultUseCase: GetLessonTemplateResultUseCase,
    private val getQuizTemplateResultUseCase: GetQuizTemplateResultUseCase,
    private val submitQuizScoreResultUseCase: SubmitQuizScoreResultUseCase,
    private val completeLessonResultUseCase: CompleteLessonResultUseCase,
    private val completeExerciseResultUseCase: CompleteExerciseResultUseCase,
    private val getQuizScoreResultUseCase: GetQuizScoreResultUseCase,
    private val authTokenManager: AuthTokenManager
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

    private val _taskProgress = MutableStateFlow<TaskProgressResponse?>(null)
    val taskProgress: StateFlow<TaskProgressResponse?> = _taskProgress

    private val _lessonTemplate = MutableStateFlow<LessonTemplate?>(null)
    val lessonTemplate: StateFlow<LessonTemplate?> = _lessonTemplate

    private val _quizTemplate = MutableStateFlow<QuizTemplate?>(null)
    val quizTemplate: StateFlow<QuizTemplate?> = _quizTemplate

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadTasksForClassroom(classroomId: String) {
        viewModelScope.launch {
            when (val result = getTasksForClassroomResultUseCase(classroomId)) {
                is Result.Success -> _tasks.value = result.data
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun loadTaskDetails(taskId: String) {
        viewModelScope.launch {
            when (val result = getTaskDetailsResultUseCase(taskId)) {
                is Result.Success -> _selectedTask.value = result.data
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun loadTaskProgress(taskId: String) {
        viewModelScope.launch {
            when (val result = getTaskProgressResultUseCase(taskId)) {
                is Result.Success -> _taskProgress.value = result.data
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun loadTaskProgressWithSuppressError(taskId: String) {
        viewModelScope.launch {
            when (val result = getTaskProgressResultUseCase(taskId)) {
                is Result.Success -> _taskProgress.value = result.data
                is Result.Error -> {
                    // Suppress error to avoid showing 403 banners
                    android.util.Log.w("TaskViewModel", "Failed to load task progress for $taskId: ${result.message}")
                }
            }
        }
    }

    fun loadLessonTemplate(lessonTemplateId: String) {
        viewModelScope.launch {
            when (val result = getLessonTemplateResultUseCase(lessonTemplateId)) {
                is Result.Success -> _lessonTemplate.value = result.data
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun loadQuizTemplate(quizTemplateId: String) {
        viewModelScope.launch {
            when (val result = getQuizTemplateResultUseCase(quizTemplateId)) {
                is Result.Success -> _quizTemplate.value = result.data
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun submitQuizScore(taskId: String, quizTemplateId: String, score: Int) {
        viewModelScope.launch {
            when (val result = submitQuizScoreResultUseCase(taskId, quizTemplateId, score)) {
                is Result.Success -> {
                    // Mark task as completed in cache for immediate UI feedback
                    TaskCompletionCache.markCompleted(taskId)
                    loadTaskProgress(taskId)
                }
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun completeLesson(taskId: String, lessonTemplateId: String) {
        viewModelScope.launch {
            when (val result = completeLessonResultUseCase(taskId, lessonTemplateId)) {
                is Result.Success -> {
                    // Mark task as completed in cache for immediate UI feedback
                    TaskCompletionCache.markCompleted(taskId)
                    loadTaskProgress(taskId)
                }
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun completeExercise(taskId: String, exerciseTemplateId: String) {
        viewModelScope.launch {
            when (val result = completeExerciseResultUseCase(taskId, exerciseTemplateId)) {
                is Result.Success -> {
                    // Mark task as completed in cache for immediate UI feedback
                    TaskCompletionCache.markCompleted(taskId)
                    loadTaskProgress(taskId)
                }
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
    
    fun refreshTaskProgress(taskId: String) {
        viewModelScope.launch {
            when (val result = getTaskProgressResultUseCase(taskId)) {
                is Result.Success -> _taskProgress.value = result.data
                is Result.Error -> {
                    // Suppress error to avoid showing banners during refresh
                    android.util.Log.w("TaskViewModel", "Failed to refresh task progress for $taskId: ${result.message}")
                }
            }
        }
    }

    fun refreshTaskProgressWithScores(taskId: String, quizTemplateId: String?) {
        viewModelScope.launch {
            // Get student ID from AuthTokenManager
            val studentId = authTokenManager.physicalIdFlow.firstOrNull()
            if (studentId.isNullOrBlank()) {
                // Fallback to basic refresh if no student ID
                refreshTaskProgress(taskId)
                return@launch
            }
            
            // First get the basic progress
            when (val result = getTaskProgressResultUseCase(taskId)) {
                is Result.Success -> {
                    var progress = result.data
                    
                    // If we have a quiz template, try to get the specific quiz score
                    if (!quizTemplateId.isNullOrBlank()) {
                        try {
                            val scoreResult = getQuizScoreResultUseCase(studentId, taskId, quizTemplateId)
                            if (scoreResult is Result.Success) {
                                val score = scoreResult.data
                                // Update progress with the latest score data
                                progress = progress.copy(
                                    quizScore = score.score ?: 0,
                                    quizCompleted = true,
                                    quizAttempts = score.attempts ?: 1 // Use attempts from score or default to 1
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.w("TaskViewModel", "Failed to fetch quiz score: ${e.message}")
                        }
                    }
                    
                    _taskProgress.value = progress
                }
                is Result.Error -> {
                    android.util.Log.w("TaskViewModel", "Failed to refresh task progress for $taskId: ${result.message}")
                }
            }
        }
    }
    
    suspend fun getTaskProgress(taskId: String, suppressError: Boolean = false): TaskProgressResponse? {
        return when (val result = getTaskProgressResultUseCase(taskId)) {
            is Result.Success -> result.data
            is Result.Error -> {
                if (!suppressError) {
                    _error.value = result.message
                }
                null
            }
        }
    }
} 