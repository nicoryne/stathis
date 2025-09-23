package citu.edu.stathis.mobile.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.data.repository.ClassroomRepository
import citu.edu.stathis.mobile.features.progress.data.model.Achievement
import citu.edu.stathis.mobile.features.progress.data.model.ProgressActivity
import citu.edu.stathis.mobile.features.progress.data.model.StudentProgress
import citu.edu.stathis.mobile.features.progress.domain.repository.ProgressRepository
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.repository.TaskRepository
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the dashboard screen
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val classroomRepository: ClassroomRepository,
    private val taskRepository: TaskRepository,
    private val vitalsRepository: VitalsRepository
) : ViewModel() {

    // Student progress state
    private val _progressState = MutableStateFlow<ProgressState>(ProgressState.Loading)
    val progressState: StateFlow<ProgressState> = _progressState.asStateFlow()
    
    // Recent achievements state
    private val _achievementsState = MutableStateFlow<List<Achievement>>(emptyList())
    val achievementsState: StateFlow<List<Achievement>> = _achievementsState.asStateFlow()
    
    // Recent activities state
    private val _activitiesState = MutableStateFlow<List<ProgressActivity>>(emptyList())
    val activitiesState: StateFlow<List<ProgressActivity>> = _activitiesState.asStateFlow()
    
    // Classrooms state
    private val _classroomsState = MutableStateFlow<ClassroomsState>(ClassroomsState.Loading)
    val classroomsState: StateFlow<ClassroomsState> = _classroomsState.asStateFlow()
    
    // Upcoming tasks state
    private val _tasksState = MutableStateFlow<TasksState>(TasksState.Loading)
    val tasksState: StateFlow<TasksState> = _tasksState.asStateFlow()
    
    // Vitals state
    private val _vitalsState = MutableStateFlow<VitalsState>(VitalsState.Loading)
    val vitalsState: StateFlow<VitalsState> = _vitalsState.asStateFlow()
    
    /**
     * Initializes the dashboard by loading all necessary data
     */
    fun initializeDashboard() {
        loadStudentProgress()
        loadClassrooms()
        loadUpcomingTasks()
        loadVitals()
    }
    
    /**
     * Loads the student's overall progress
     */
    private fun loadStudentProgress() {
        viewModelScope.launch {
            _progressState.value = ProgressState.Loading
            
            try {
                progressRepository.getStudentProgress()
                    .catch { e ->
                        Timber.e(e, "Error loading student progress")
                        _progressState.value = ProgressState.Error(e.message ?: "Unknown error")
                    }
                    .collectLatest { response ->
                        if (response.success && response.data != null) {
                            val progress = response.data
                            _progressState.value = ProgressState.Success(progress)
                            
                            // Get recent achievements if available
                            val achievements = progress.achievements ?: emptyList()
                            _achievementsState.value = achievements.take(3) // Take only the 3 most recent
                            
                            // Get recent activities if available
                            val activities = progress.recentActivities ?: emptyList()
                            _activitiesState.value = activities.take(5) // Take only the 5 most recent
                        } else {
                            _progressState.value = ProgressState.Error(response.message ?: "Unknown error")
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading student progress")
                _progressState.value = ProgressState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Loads the student's classrooms
     */
    private fun loadClassrooms() {
        viewModelScope.launch {
            _classroomsState.value = ClassroomsState.Loading
            
            try {
                classroomRepository.getStudentClassrooms()
                    .catch { e ->
                        Timber.e(e, "Error loading classrooms")
                        _classroomsState.value = ClassroomsState.Error(e.message ?: "Unknown error")
                    }
                    .collectLatest { classrooms ->
                        if (classrooms.isEmpty()) {
                            _classroomsState.value = ClassroomsState.Empty
                        } else {
                            _classroomsState.value = ClassroomsState.Success(classrooms)
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading classrooms")
                _classroomsState.value = ClassroomsState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Loads upcoming tasks from all classrooms
     */
    private fun loadUpcomingTasks() {
        viewModelScope.launch {
            _tasksState.value = TasksState.Loading
            
            try {
                // First get all classrooms
                val classrooms = classroomRepository.getStudentClassrooms().first()
                
                // Then get tasks for each classroom
                val allTasks = mutableListOf<Task>()
                for (classroom in classrooms) {
                    val tasks = classroomRepository.getClassroomTasks(classroom.physicalId).first()
                    allTasks.addAll(tasks)
                }
                
                // Sort by closing date and take the 5 most urgent
                val upcomingTasks = allTasks
                    .sortedBy { it.closingDate }
                    .take(5)
                
                if (upcomingTasks.isEmpty()) {
                    _tasksState.value = TasksState.Empty
                } else {
                    _tasksState.value = TasksState.Success(upcomingTasks)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading upcoming tasks")
                _tasksState.value = TasksState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Loads the student's vitals data
     */
    private fun loadVitals() {
        viewModelScope.launch {
            _vitalsState.value = VitalsState.Loading
            
            try {
                // TODO: Implement vitals data loading from the data layer repository
                // The current data layer VitalsRepository doesn't have observe methods
                // For now, provide mock data to prevent build errors
                _vitalsState.value = VitalsState.Success(
                    heartRate = 72f,
                    oxygenSaturation = 98f,
                    temperature = 36.5f
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading vitals")
                _vitalsState.value = VitalsState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Refreshes all dashboard data
     */
    fun refreshDashboard() {
        initializeDashboard()
    }
}

/**
 * Sealed class representing the different states of the progress UI
 */
sealed class ProgressState {
    object Loading : ProgressState()
    data class Success(val progress: StudentProgress) : ProgressState()
    data class Error(val message: String) : ProgressState()
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
 * Sealed class representing the different states of the tasks UI
 */
sealed class TasksState {
    object Loading : TasksState()
    object Empty : TasksState()
    data class Success(val tasks: List<Task>) : TasksState()
    data class Error(val message: String) : TasksState()
}

/**
 * Sealed class representing the different states of the vitals UI
 */
sealed class VitalsState {
    object Loading : VitalsState()
    data class Success(
        val heartRate: Float,
        val oxygenSaturation: Float,
        val temperature: Float
    ) : VitalsState()
    data class Error(val message: String) : VitalsState()
}
