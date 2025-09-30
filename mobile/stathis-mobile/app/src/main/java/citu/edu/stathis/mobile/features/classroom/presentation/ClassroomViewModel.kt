package citu.edu.stathis.mobile.features.classroom.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.data.repository.ClassroomRepository
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassroomViewModel @Inject constructor(
    private val classroomRepository: ClassroomRepository
) : ViewModel() {

    private val _classrooms = MutableStateFlow<List<Classroom>>(emptyList())
    val classrooms: StateFlow<List<Classroom>> = _classrooms

    private val _selectedClassroom = MutableStateFlow<Classroom?>(null)
    val selectedClassroom: StateFlow<Classroom?> = _selectedClassroom

    private val _classroomTasks = MutableStateFlow<List<Task>>(emptyList())
    val classroomTasks: StateFlow<List<Task>> = _classroomTasks

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadStudentClassrooms() {
        viewModelScope.launch {
            classroomRepository.getStudentClassrooms()
                .catch { e ->
                    _error.value = e.message
                }
                .collect { classrooms ->
                    _classrooms.value = classrooms
                }
        }
    }

    fun loadClassroomDetails(classroomId: String) {
        viewModelScope.launch {
            classroomRepository.getStudentClassroom(classroomId)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { classroom ->
                    _selectedClassroom.value = classroom
                    loadClassroomTasks(classroomId)
                }
        }
    }

    private fun loadClassroomTasks(classroomId: String) {
        viewModelScope.launch {
            classroomRepository.getClassroomTasks(classroomId)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { tasks ->
                    _classroomTasks.value = tasks
                }
        }
    }

    fun enrollInClassroom(classroomCode: String) {
        viewModelScope.launch {
            classroomRepository.enrollInClassroom(classroomCode)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { classroom ->
                    loadStudentClassrooms()
                    _selectedClassroom.value = classroom
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 