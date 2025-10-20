package citu.edu.stathis.mobile.features.classroom.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.domain.usecase.EnrollInClassroomUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetClassroomDetailsUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetClassroomTasksUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetStudentClassroomsResultUseCase
import citu.edu.stathis.mobile.features.common.domain.Result
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.profile.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
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
    private val getClassroomTasksUseCase: GetClassroomTasksUseCase,
    private val classroomService: citu.edu.stathis.mobile.features.classroom.data.api.ClassroomService,
    private val authTokenManager: citu.edu.stathis.mobile.core.data.AuthTokenManager,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // UI state for classrooms
    private val _classroomsState = MutableStateFlow<ClassroomsState>(ClassroomsState.Loading)
    val classroomsState: StateFlow<ClassroomsState> = _classroomsState.asStateFlow()
    private val _verifiedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val verifiedMap: StateFlow<Map<String, Boolean>> = _verifiedMap.asStateFlow()
    
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
            
            // First ensure we have the user's physicalId by loading profile if needed
            val currentPhysicalId = authTokenManager.physicalIdFlow.firstOrNull()
            if (currentPhysicalId.isNullOrBlank()) {
                Timber.d("No physicalId found, this might be a demo user or profile not loaded yet")
            }
            
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
                                    // Fetch verification flags for current user in these classrooms
                                    var me = authTokenManager.physicalIdFlow.firstOrNull()
                                    Timber.d("Current user ID (from token store): $me")
                                    // If we don't have a physicalId yet, fetch profile to resolve identity once
                                    if (me.isNullOrBlank()) {
                                        runCatching { profileRepository.getUserProfile() }.onSuccess { resp ->
                                            if (resp.success && resp.data != null) {
                                                val profile = resp.data
                                                // Persist physicalId for future lookups
                                                runCatching {
                                                    viewModelScope.launch {
                                                        authTokenManager.updateUserIdentity(profile.physicalId, profile.role)
                                                    }
                                                }
                                                me = profile.physicalId
                                                Timber.d("Resolved user ID from profile: $me")
                                            }
                                        }.onFailure { Timber.w(it, "Failed to resolve profile for user id; will still attempt email match below") }
                                    }
                                    
                                    if (!me.isNullOrBlank()) {
                                        val map = mutableMapOf<String, Boolean>()
                                        // Fetch actual verification status from API; default to false
                                        for (c in classrooms) {
                                            try {
                                                Timber.d("Fetching students for classroom: ${c.physicalId}")
                                                val resp = classroomService.getStudentsForClassroom(c.physicalId)
                                                if (resp.isSuccessful) {
                                                    val list = resp.body().orEmpty()
                                                    Timber.d("Students in classroom ${c.physicalId}: $list")
                                                    // Match by physicalId primarily. If not available yet, fall back to best-effort: if there's exactly one entry and it's verified, assume it's this user.
                                                    val entry = list.firstOrNull { it.physicalId.equals(me, ignoreCase = true) }
                                                        ?: if (me.isNullOrBlank() && list.size == 1) list.first() else null
                                                    val isVerified = entry?.verified == true
                                                    map[c.physicalId] = isVerified
                                                    Timber.d("Verification for ${c.physicalId}: $isVerified")
                                                } else {
                                                    Timber.e("Failed to fetch students for classroom ${c.physicalId}: ${resp.code()} ${resp.message()}")
                                                    map[c.physicalId] = false
                                                }
                                            } catch (e: Exception) { 
                                                Timber.e(e, "Exception fetching students for classroom ${c.physicalId}")
                                                map[c.physicalId] = false
                                            }
                                        }
                                        Timber.d("Final verification map: $map")
                                        _verifiedMap.value = map
                                    } else {
                                        Timber.w("User ID is null or blank and profile fetch failed; temporarily mark all classrooms as pending and retry later")
                                        _verifiedMap.value = classrooms.associate { it.physicalId to false }
                                    }
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
                            e.message?.contains("not found") == true || 
                            e.message?.contains("Classroom not found") == true -> 
                                "Classroom not found. Please check the code and try again."
                            e.message?.contains("already enrolled") == true || 
                            e.message?.contains("Student is already enrolled") == true -> 
                                "You are already enrolled in this classroom."
                            e.message?.contains("not active") == true || 
                            e.message?.contains("Classroom is not active") == true -> 
                                "This classroom is not currently active. Please contact your teacher."
                            e.message?.contains("Enrollment failed") == true -> 
                                "Enrollment failed. Please check your internet connection and try again."
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
                    e.message?.contains("not found") == true || 
                    e.message?.contains("Classroom not found") == true -> 
                        "Classroom not found. Please check the code and try again."
                    e.message?.contains("already enrolled") == true || 
                    e.message?.contains("Student is already enrolled") == true -> 
                        "You are already enrolled in this classroom."
                    e.message?.contains("not active") == true || 
                    e.message?.contains("Classroom is not active") == true -> 
                        "This classroom is not currently active. Please contact your teacher."
                    e.message?.contains("Enrollment failed") == true -> 
                        "Enrollment failed. Please check your internet connection and try again."
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
