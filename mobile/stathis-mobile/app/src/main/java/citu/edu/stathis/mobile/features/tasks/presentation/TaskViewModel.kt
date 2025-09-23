package citu.edu.stathis.mobile.features.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.domain.usecase.*
import citu.edu.stathis.mobile.features.common.domain.Result
import citu.edu.stathis.mobile.features.common.domain.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTasksForClassroomResultUseCase: GetTasksForClassroomResultUseCase,
    private val getTaskDetailsResultUseCase: GetTaskDetailsResultUseCase,
    private val getTaskProgressResultUseCase: GetTaskProgressResultUseCase,
    private val submitQuizScoreResultUseCase: SubmitQuizScoreResultUseCase,
    private val completeLessonResultUseCase: CompleteLessonResultUseCase,
    private val completeExerciseResultUseCase: CompleteExerciseResultUseCase
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

    private val _taskProgress = MutableStateFlow<TaskProgressResponse?>(null)
    val taskProgress: StateFlow<TaskProgressResponse?> = _taskProgress

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

    fun submitQuizScore(taskId: String, quizTemplateId: String, score: Int) {
        viewModelScope.launch {
            when (val result = submitQuizScoreResultUseCase(taskId, quizTemplateId, score)) {
                is Result.Success -> loadTaskProgress(taskId)
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun completeLesson(taskId: String, lessonTemplateId: String) {
        viewModelScope.launch {
            when (val result = completeLessonResultUseCase(taskId, lessonTemplateId)) {
                is Result.Success -> loadTaskProgress(taskId)
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun completeExercise(taskId: String, exerciseTemplateId: String) {
        viewModelScope.launch {
            when (val result = completeExerciseResultUseCase(taskId, exerciseTemplateId)) {
                is Result.Success -> loadTaskProgress(taskId)
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 