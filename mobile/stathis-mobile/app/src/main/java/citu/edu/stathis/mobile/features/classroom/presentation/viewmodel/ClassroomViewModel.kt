package citu.edu.stathis.mobile.features.classroom.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.data.model.ClassroomProgress
import citu.edu.stathis.mobile.features.classroom.domain.usecase.EnrollInClassroomUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetClassroomDetailsUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetClassroomProgressUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetClassroomTasksUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetStudentClassroomsResultUseCase
import citu.edu.stathis.mobile.features.common.domain.Result
import citu.edu.stathis.mobile.features.tasks.data.model.Task
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
 * ViewModel for the classroom management feature
 */
@HiltViewModel
class ClassroomViewModel @Inject constructor(
    private val getStudentClassroomsResult: GetStudentClassroomsResultUseCase,
    private val enrollInClassroomUseCase: EnrollInClassroomUseCase,
    private val getClassroomDetailsUseCase: GetClassroomDetailsUseCase,
    private val getClassroomProgressUseCase: GetClassroomProgressUseCase,
    private val getClassroomTasksUseCase: GetClassroomTasksUseCase
) : ViewModel() {

    // UI state for classrooms
    private val _classroomsState = MutableStateFlow<ClassroomsState>(ClassroomsState.Loading)
    val classroomsState: StateFlow<ClassroomsState> = _classroomsState.asStateFlow()
    
    // UI state for enrollment
    private val _enrollmentState = MutableStateFlow<EnrollmentState>(EnrollmentState.Idle)
    val enrollmentState: StateFlow<EnrollmentState> = _enrollmentState.asStateFlow()
    
    // UI state for classroom tasks
    private val _tasksState = MutableStateFlow<TasksState>(TasksState.Loading)
    val tasksState: StateFlow<TasksState> = _tasksState.asStateFlow()
    
    // Selected classroom
    private val _selectedClassroom = MutableStateFlow<Classroom?>(null)
    val selectedClassroom: StateFlow<Classroom?> = _selectedClassroom.asStateFlow()
    
    /**
     * Loads all classrooms for the current student
     */
    fun loadStudentClassrooms() {
        viewModelScope.launch {
            _classroomsState.value = ClassroomsState.Loading
            
            try {
                getStudentClassroomsResult()
                    .catch { e ->
                        Timber.e(e, "Error loading student classrooms")
                        
                        // Provide more user-friendly error messages
                        val errorMessage = when {
                            e.message?.contains("403") == true -> 
                                "You don't have access to any classrooms yet. Try enrolling in a classroom first."
                            e.message?.contains("401") == true -> 
                                "You need to log in to view your classrooms."
                            else -> e.message ?: "Unable to load classrooms. Please try again."
                        }
                        
                        // For 403, treat it as an empty state rather than an error
                        if (e.message?.contains("403") == true) {
                            _classroomsState.value = ClassroomsState.Empty
                        } else {
                            _classroomsState.value = ClassroomsState.Error(errorMessage)
                        }
                    }
                    .collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                val classrooms = result.data
                                if (classrooms.isEmpty()) {
                                    _classroomsState.value = ClassroomsState.Empty
                                } else {
                                    _classroomsState.value = ClassroomsState.Success(classrooms)
                                }
                            }
                            is Result.Error -> {
                                _classroomsState.value = ClassroomsState.Error(result.message)
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading student classrooms")
                
                // Handle errors consistently
                val errorMessage = when {
                    e.message?.contains("403") == true -> 
                        "You don't have access to any classrooms yet. Try enrolling in a classroom first."
                    e.message?.contains("401") == true -> 
                        "You need to log in to view your classrooms."
                    else -> e.message ?: "Unable to load classrooms. Please try again."
                }
                
                // For 403, treat it as an empty state rather than an error
                if (e.message?.contains("403") == true) {
                    _classroomsState.value = ClassroomsState.Empty
                } else {
                    _classroomsState.value = ClassroomsState.Error(errorMessage)
                }
            }
        }
    }
    
    /**
     * Enrolls the student in a classroom using a classroom code
     */
    fun enrollInClassroom(classroomCode: String) {
        viewModelScope.launch {
            _enrollmentState.value = EnrollmentState.Enrolling
            
            try {
                enrollInClassroomUseCase(classroomCode)
                    .catch { e ->
                        Timber.e(e, "Error enrolling in classroom")
                        
                        // Provide user-friendly error messages
                        val errorMessage = when {
                            e.message?.contains("403") == true -> 
                                "You don't have permission to join this classroom. Please check the code or contact your teacher."
                            e.message?.contains("404") == true || 
                            e.message?.contains("invalid") == true || 
                            e.message?.contains("not found") == true -> 
                                "Classroom not found. Please check the code and try again."
                            e.message?.contains("already enrolled") == true -> 
                                "You are already enrolled in this classroom."
                            else -> e.message ?: "Failed to enroll in classroom. Please try again."
                        }
                        
                        _enrollmentState.value = EnrollmentState.Error(errorMessage)
                    }
                    .collectLatest { classroom ->
                        _enrollmentState.value = EnrollmentState.Success(classroom)
                        loadStudentClassrooms()
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error enrolling in classroom")
                
                // Same error handling logic for consistency
                val errorMessage = when {
                    e.message?.contains("403") == true -> 
                        "You don't have permission to join this classroom. Please check the code or contact your teacher."
                    e.message?.contains("404") == true || 
                    e.message?.contains("invalid") == true || 
                    e.message?.contains("not found") == true -> 
                        "Classroom not found. Please check the code and try again."
                    e.message?.contains("already enrolled") == true -> 
                        "You are already enrolled in this classroom."
                    else -> e.message ?: "Failed to enroll in classroom. Please try again."
                }
                
                _enrollmentState.value = EnrollmentState.Error(errorMessage)
            }
        }
    }
    
    /**
     * Loads all tasks for a specific classroom
     */
    fun loadClassroomTasks(classroomId: String) {
        viewModelScope.launch {
            _tasksState.value = TasksState.Loading
            
            try {
                getClassroomTasksUseCase(classroomId)
                    .catch { e ->
                        Timber.e(e, "Error loading classroom tasks")
                        _tasksState.value = TasksState.Error(e.message ?: "Unknown error")
                    }
                    .collectLatest { tasks ->
                        if (tasks.isEmpty()) {
                            _tasksState.value = TasksState.Empty
                        } else {
                            _tasksState.value = TasksState.Success(tasks)
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading classroom tasks")
                _tasksState.value = TasksState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Sets the selected classroom
     */
    fun selectClassroom(classroom: Classroom) {
        _selectedClassroom.value = classroom
        loadClassroomTasks(classroom.physicalId)
    }
    
    /**
     * Resets the enrollment state
     */
    fun resetEnrollmentState() {
        _enrollmentState.value = EnrollmentState.Idle
    }
}

/**
 * Sealed class representing the different states of the classrooms UI
 */
sealed class ClassroomsState {
    object Loading : ClassroomsState()
    object Empty : ClassroomsState()
    data class Success(val classrooms: List<Classroom>) : ClassroomsState()
    data class Error(val message: String) : ClassroomsState()
}

/**
 * Sealed class representing the different states of the enrollment UI
 */
sealed class EnrollmentState {
    object Idle : EnrollmentState()
    object Enrolling : EnrollmentState()
    data class Success(val classroom: Classroom) : EnrollmentState()
    data class Error(val message: String) : EnrollmentState()
}

/**
 * Sealed class representing the different states of the tasks UI
 */
sealed class TasksState {
    object Loading : TasksState()
    object Empty : TasksState()
    data class Success(val tasks: List<Task>) : TasksState()
    data class Error(val message: String) : TasksState()
}
