package citu.edu.stathis.mobile.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.tasks.data.model.ScoreResponse
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the task detail screen
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    // UI state for task details
    private val _taskState = MutableStateFlow<TaskState>(TaskState.Loading)
    val taskState: StateFlow<TaskState> = _taskState.asStateFlow()
    
    // UI state for task progress
    private val _progressState = MutableStateFlow<ProgressState>(ProgressState.Loading)
    val progressState: StateFlow<ProgressState> = _progressState.asStateFlow()
    
    // UI state for quiz score submission
    private val _scoreState = MutableStateFlow<ScoreState>(ScoreState.Idle)
    val scoreState: StateFlow<ScoreState> = _scoreState.asStateFlow()
    
    /**
     * Loads the task details
     */
    fun loadTaskDetails(taskId: String) {
        viewModelScope.launch {
            _taskState.value = TaskState.Loading
            
            try {
                taskRepository.getStudentTask(taskId)
                    .catch { e ->
                        Timber.e(e, "Error loading task details")
                        _taskState.value = TaskState.Error(e.message ?: "Unknown error")
                    }
                    .collectLatest { task ->
                        _taskState.value = TaskState.Success(task)
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading task details")
                _taskState.value = TaskState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Loads the task progress
     */
    fun loadTaskProgress(taskId: String) {
        viewModelScope.launch {
            _progressState.value = ProgressState.Loading
            
            try {
                taskRepository.getTaskProgress(taskId)
                    .catch { e ->
                        Timber.e(e, "Error loading task progress")
                        _progressState.value = ProgressState.Error(e.message ?: "Unknown error")
                    }
                    .collectLatest { progress ->
                        _progressState.value = ProgressState.Success(progress)
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading task progress")
                _progressState.value = ProgressState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Submits a quiz score
     */
    fun submitQuizScore(taskId: String, quizTemplateId: String, score: Int) {
        viewModelScope.launch {
            _scoreState.value = ScoreState.Submitting
            
            try {
                taskRepository.submitQuizScore(taskId, quizTemplateId, score)
                    .catch { e ->
                        Timber.e(e, "Error submitting quiz score")
                        _scoreState.value = ScoreState.Error(e.message ?: "Unknown error")
                    }
                    .collectLatest { response ->
                        _scoreState.value = ScoreState.Success(response)
                        // Refresh task details and progress
                        loadTaskDetails(taskId)
                        loadTaskProgress(taskId)
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error submitting quiz score")
                _scoreState.value = ScoreState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Completes a lesson
     */
    fun completeLesson(taskId: String, lessonTemplateId: String) {
        viewModelScope.launch {
            try {
                taskRepository.completeLesson(taskId, lessonTemplateId)
                // Refresh task details and progress
                loadTaskDetails(taskId)
                loadTaskProgress(taskId)
            } catch (e: Exception) {
                Timber.e(e, "Error completing lesson")
            }
        }
    }
    
    /**
     * Completes an exercise
     */
    fun completeExercise(taskId: String, exerciseTemplateId: String) {
        viewModelScope.launch {
            try {
                taskRepository.completeExercise(taskId, exerciseTemplateId)
                // Refresh task details and progress
                loadTaskDetails(taskId)
                loadTaskProgress(taskId)
            } catch (e: Exception) {
                Timber.e(e, "Error completing exercise")
            }
        }
    }
    
    /**
     * Resets the score state
     */
    fun resetScoreState() {
        _scoreState.value = ScoreState.Idle
    }
}

/**
 * Sealed class representing the different states of the task UI
 */
sealed class TaskState {
    object Loading : TaskState()
    data class Success(val task: Task) : TaskState()
    data class Error(val message: String) : TaskState()
}

/**
 * Sealed class representing the different states of the progress UI
 */
sealed class ProgressState {
    object Loading : ProgressState()
    data class Success(val progress: TaskProgressResponse) : ProgressState()
    data class Error(val message: String) : ProgressState()
}

/**
 * Sealed class representing the different states of the score submission UI
 */
sealed class ScoreState {
    object Idle : ScoreState()
    object Submitting : ScoreState()
    data class Success(val response: ScoreResponse) : ScoreState()
    data class Error(val message: String) : ScoreState()
}
