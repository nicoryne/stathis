package citu.edu.stathis.mobile.features.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
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
            taskRepository.getStudentTasksForClassroom(classroomId)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { tasks ->
                    _tasks.value = tasks
                }
        }
    }

    fun loadTaskDetails(taskId: String) {
        viewModelScope.launch {
            taskRepository.getStudentTask(taskId)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { task ->
                    _selectedTask.value = task
                }
        }
    }

    fun loadTaskProgress(taskId: String) {
        viewModelScope.launch {
            taskRepository.getTaskProgress(taskId)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { progress ->
                    _taskProgress.value = progress
                }
        }
    }

    fun submitQuizScore(taskId: String, quizTemplateId: String, score: Int) {
        viewModelScope.launch {
            taskRepository.submitQuizScore(taskId, quizTemplateId, score)
                .catch { e ->
                    _error.value = e.message
                }
                .collect {
                    loadTaskProgress(taskId)
                }
        }
    }

    fun completeLesson(taskId: String, lessonTemplateId: String) {
        viewModelScope.launch {
            try {
                taskRepository.completeLesson(taskId, lessonTemplateId)
                loadTaskProgress(taskId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun completeExercise(taskId: String, exerciseTemplateId: String) {
        viewModelScope.launch {
            try {
                taskRepository.completeExercise(taskId, exerciseTemplateId)
                loadTaskProgress(taskId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 