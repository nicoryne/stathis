package citu.edu.stathis.mobile.features.vitals.data.repository

import android.util.Log
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsRestRequestDto
import citu.edu.stathis.mobile.features.vitals.data.service.VitalsRestApiService
import citu.edu.stathis.mobile.features.vitals.data.utils.VitalsChangeDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling vital signs REST API operations
 * Includes change detection to minimize API requests
 */
@Singleton
class VitalsRestRepository @Inject constructor(
    private val vitalsRestApiService: VitalsRestApiService
) {
    private val TAG = "VitalsRestRepository"
    
    private val changeDetector = VitalsChangeDetector()
    
    private val _postingState = MutableStateFlow(VitalsPostingState.IDLE)
    val postingState: StateFlow<VitalsPostingState> = _postingState.asStateFlow()
    
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    
    /**
     * Posts vital signs data to the REST API if there are significant changes
     * @param vitalSigns The vital signs data to post
     * @param physicalId The physical ID of the user
     * @param studentId The student ID
     * @param classroomId The classroom ID (optional)
     * @param taskId The task ID (optional)
     * @param isPreActivity Whether this is pre-activity vitals
     * @param isPostActivity Whether this is post-activity vitals
     * @param forcePost Whether to force posting regardless of change detection
     * @return ClientResponse indicating success or failure
     */
    suspend fun postVitalsIfChanged(
        vitalSigns: VitalSigns,
        physicalId: String,
        studentId: String,
        classroomId: String,
        taskId: String,
        isPreActivity: Boolean = false,
        isPostActivity: Boolean = false,
        forcePost: Boolean = false
    ): ClientResponse<Unit> {
        return try {
            // Check if we should post based on change detection
            if (!forcePost && !changeDetector.hasSignificantChange(vitalSigns)) {
                Log.d(TAG, "No significant change detected, skipping post")
                return ClientResponse(success = true, message = "No significant change", data = Unit)
            }
            
            _postingState.value = VitalsPostingState.POSTING
            
            val requestDto = VitalsRestRequestDto.fromVitalSigns(
                vitalSigns = vitalSigns,
                physicalId = physicalId,
                studentId = studentId,
                classroomId = classroomId,
                taskId = taskId,
                isPreActivity = isPreActivity,
                isPostActivity = isPostActivity
            )
            
            Log.d(TAG, "=== VITALS POST REQUEST DEBUG ===")
            Log.d(TAG, "Raw VitalSigns: $vitalSigns")
            Log.d(TAG, "Request DTO: $requestDto")
            Log.d(TAG, "Physical ID: $physicalId")
            Log.d(TAG, "Student ID: $studentId")
            Log.d(TAG, "Classroom ID: $classroomId")
            Log.d(TAG, "Task ID: $taskId")
            Log.d(TAG, "Is Pre-Activity: $isPreActivity")
            Log.d(TAG, "Is Post-Activity: $isPostActivity")
            Log.d(TAG, "Force Post: $forcePost")
            
            // Check for null values that might cause issues
            Log.d(TAG, "Heart Rate: ${requestDto.heartRate} (null: ${requestDto.heartRate == null})")
            Log.d(TAG, "Oxygen Saturation: ${requestDto.oxygenSaturation} (null: ${requestDto.oxygenSaturation == null})")
            Log.d(TAG, "Timestamp: ${requestDto.timestamp}")
            Log.d(TAG, "=================================")
            
            val response = vitalsRestApiService.postVitals(requestDto)
            
            Log.d(TAG, "=== VITALS POST RESPONSE DEBUG ===")
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Message: ${response.message()}")
            Log.d(TAG, "Response Headers: ${response.headers()}")
            Log.d(TAG, "Response Body: ${response.body()}")
            Log.d(TAG, "Is Successful: ${response.isSuccessful}")
            Log.d(TAG, "===================================")
            
            if (response.isSuccessful) {
                changeDetector.updateLastPostedVitals(vitalSigns)
                _postingState.value = VitalsPostingState.SUCCESS
                _lastError.value = null
                Log.d(TAG, "Successfully posted vitals")
                ClientResponse(success = true, message = "Vitals posted successfully", data = Unit)
            } else {
                val errorMessage = "Failed to post vitals: HTTP ${response.code()}"
                _postingState.value = VitalsPostingState.ERROR
                _lastError.value = errorMessage
                Log.e(TAG, errorMessage)
                ClientResponse(success = false, message = errorMessage, data = null)
            }
        } catch (e: Exception) {
            val errorMessage = "Error posting vitals: ${e.message}"
            _postingState.value = VitalsPostingState.ERROR
            _lastError.value = errorMessage
            Log.e(TAG, errorMessage, e)
            ClientResponse(success = false, message = errorMessage, data = null)
        }
    }
    
    /**
     * Posts pre-activity vitals (always posts regardless of change detection)
     */
    suspend fun postPreActivityVitals(
        vitalSigns: VitalSigns,
        physicalId: String,
        studentId: String,
        classroomId: String,
        taskId: String
    ): ClientResponse<Unit> {
        changeDetector.forceNextPost()
        return postVitalsIfChanged(
            vitalSigns = vitalSigns,
            physicalId = physicalId,
            studentId = studentId,
            classroomId = classroomId,
            taskId = taskId,
            isPreActivity = true,
            forcePost = true
        )
    }
    
    /**
     * Posts post-activity vitals (always posts regardless of change detection)
     */
    suspend fun postPostActivityVitals(
        vitalSigns: VitalSigns,
        physicalId: String,
        studentId: String,
        classroomId: String,
        taskId: String
    ): ClientResponse<Unit> {
        changeDetector.forceNextPost()
        return postVitalsIfChanged(
            vitalSigns = vitalSigns,
            physicalId = physicalId,
            studentId = studentId,
            classroomId = classroomId,
            taskId = taskId,
            isPostActivity = true,
            forcePost = true
        )
    }
    
    /**
     * Resets the change detector (useful when starting a new exercise session)
     */
    fun resetChangeDetector() {
        changeDetector.reset()
        _postingState.value = VitalsPostingState.IDLE
        _lastError.value = null
    }
    
    /**
     * Clears any error state
     */
    fun clearError() {
        _lastError.value = null
        if (_postingState.value == VitalsPostingState.ERROR) {
            _postingState.value = VitalsPostingState.IDLE
        }
    }
}

enum class VitalsPostingState {
    IDLE,
    POSTING,
    SUCCESS,
    ERROR
}

