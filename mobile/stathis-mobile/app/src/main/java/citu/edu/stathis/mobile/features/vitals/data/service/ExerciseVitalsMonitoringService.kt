package citu.edu.stathis.mobile.features.vitals.data.service

import android.util.Log
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRestRepository
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsPostingState
import citu.edu.stathis.mobile.features.vitals.domain.usecase.PostVitalsDuringExerciseUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for monitoring and posting vital signs during exercise sessions
 * Integrates with Health Connect and the REST API
 */
@Singleton
class ExerciseVitalsMonitoringService @Inject constructor(
    private val postVitalsDuringExerciseUseCase: PostVitalsDuringExerciseUseCase,
    private val vitalsRestRepository: VitalsRestRepository
) {
    private val TAG = "ExerciseVitalsMonitoringService"
    
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    val postingState: StateFlow<VitalsPostingState> = vitalsRestRepository.postingState
    val lastError: StateFlow<String?> = vitalsRestRepository.lastError
    
    private var monitoringJob: Job? = null
    private var currentClassroomId: String? = null
    private var currentTaskId: String? = null
    private var currentPhysicalId: String? = null
    private var currentStudentId: String? = null
    
    /**
     * Starts monitoring vital signs during exercise
     * @param classroomId The classroom ID
     * @param taskId The task ID
     * @param vitalsProvider Function that provides current vital signs
     * @param scope Coroutine scope for the monitoring job
     */
    fun startMonitoring(
        classroomId: String,
        taskId: String,
        physicalId: String,
        studentId: String,
        vitalsProvider: () -> VitalSigns?,
        scope: CoroutineScope
    ) {
        if (_isMonitoring.value) {
            Log.w(TAG, "Already monitoring, stopping previous session")
            stopMonitoring()
        }
        
        currentClassroomId = classroomId
        currentTaskId = taskId
        currentPhysicalId = physicalId
        currentStudentId = studentId
        
        // Reset change detector for new session
        postVitalsDuringExerciseUseCase.resetForNewSession()
        
        _isMonitoring.value = true
        
        Log.d(TAG, "Starting vitals monitoring for classroom: $classroomId, task: $taskId")
        
        monitoringJob = scope.launch {
            // First, post pre-activity vitals
            try {
                val initialVitals = vitalsProvider()
                if (initialVitals != null) {
                    Log.d(TAG, "Posting pre-activity vitals: HR=${initialVitals.heartRate}, O2=${initialVitals.oxygenSaturation}")
                    val preActivityResult = postPreActivityVitals(initialVitals)
                    if (preActivityResult.success) {
                        Log.d(TAG, "Pre-activity vitals posted successfully")
                    } else {
                        Log.w(TAG, "Failed to post pre-activity vitals: ${preActivityResult.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error posting pre-activity vitals", e)
            }
            
            // Then start continuous monitoring - force POST every second
            while (_isMonitoring.value) {
                try {
                    val vitals = vitalsProvider()
                    if (vitals != null) {
                        Log.d(TAG, "Force posting vitals every second: HR=${vitals.heartRate}, O2=${vitals.oxygenSaturation}")
                        
                        // Force POST every second, bypassing change detection
                        val result = forcePostVitals(vitals)
                        
                        if (result.success) {
                            Log.d(TAG, "Vitals force posted successfully")
                        } else {
                            Log.w(TAG, "Failed to force post vitals: ${result.message}")
                        }
                    } else {
                        Log.d(TAG, "No vitals data available")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during vitals monitoring", e)
                }
                
                // Force update every second for real-time monitoring
                delay(1000) // 1 second
            }
        }
    }
    
    /**
     * Stops monitoring vital signs
     */
    fun stopMonitoring() {
        Log.d(TAG, "Stopping vitals monitoring")
        _isMonitoring.value = false
        monitoringJob?.cancel()
        monitoringJob = null
        currentClassroomId = null
        currentTaskId = null
        currentPhysicalId = null
        currentStudentId = null
    }
    
    /**
     * Stops monitoring and posts post-activity vitals
     */
    suspend fun stopMonitoringWithPostActivity(vitals: VitalSigns) {
        Log.d(TAG, "Stopping vitals monitoring and posting post-activity vitals")
        
        // Stop monitoring first
        _isMonitoring.value = false
        monitoringJob?.cancel()
        monitoringJob = null
        
        // Post post-activity vitals
        try {
            Log.d(TAG, "Posting post-activity vitals: HR=${vitals.heartRate}, O2=${vitals.oxygenSaturation}")
            val postActivityResult = postPostActivityVitals(vitals)
            if (postActivityResult.success) {
                Log.d(TAG, "Post-activity vitals posted successfully")
            } else {
                Log.w(TAG, "Failed to post post-activity vitals: ${postActivityResult.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error posting post-activity vitals", e)
        }
        
        // Clear session data
        currentClassroomId = null
        currentTaskId = null
        currentPhysicalId = null
        currentStudentId = null
    }
    
    /**
     * Posts pre-activity vitals (always posts)
     */
    suspend fun postPreActivityVitals(vitals: VitalSigns): ClientResponse<Unit> {
        Log.d(TAG, "Posting pre-activity vitals")
        return vitalsRestRepository.postPreActivityVitals(
            vitalSigns = vitals,
            physicalId = currentPhysicalId ?: "unknown",
            studentId = currentStudentId ?: "unknown",
            classroomId = currentClassroomId ?: "unknown",
            taskId = currentTaskId ?: "unknown"
        )
    }
    
    /**
     * Posts post-activity vitals (always posts)
     */
    suspend fun postPostActivityVitals(vitals: VitalSigns): ClientResponse<Unit> {
        Log.d(TAG, "Posting post-activity vitals")
        return vitalsRestRepository.postPostActivityVitals(
            vitalSigns = vitals,
            physicalId = currentPhysicalId ?: "unknown",
            studentId = currentStudentId ?: "unknown",
            classroomId = currentClassroomId ?: "unknown",
            taskId = currentTaskId ?: "unknown"
        )
    }
    
    /**
     * Forces posting of current vitals (bypasses change detection)
     */
    suspend fun forcePostVitals(vitals: VitalSigns): ClientResponse<Unit> {
        Log.d(TAG, "Force posting vitals")
        return vitalsRestRepository.postVitalsIfChanged(
            vitalSigns = vitals,
            physicalId = currentPhysicalId ?: "unknown",
            studentId = currentStudentId ?: "unknown",
            classroomId = currentClassroomId ?: "unknown",
            taskId = currentTaskId ?: "unknown",
            forcePost = true
        )
    }
    
    /**
     * Clears any error state
     */
    fun clearError() {
        // Error state is managed by the repository
        Log.d(TAG, "Error state cleared")
    }
}
