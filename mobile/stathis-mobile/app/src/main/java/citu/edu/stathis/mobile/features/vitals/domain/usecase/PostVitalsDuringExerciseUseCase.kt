package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRestRepository
import citu.edu.stathis.mobile.features.exercise.domain.usecase.GetCurrentUserIdUseCase
import javax.inject.Inject

/**
 * Use case for posting vital signs during exercise monitoring
 * Handles the business logic for determining when and how to post vitals
 */
class PostVitalsDuringExerciseUseCase @Inject constructor(
    private val vitalsRestRepository: VitalsRestRepository
) {
    
    /**
     * Posts vital signs data during exercise if there are significant changes
     * @param vitalSigns The vital signs data from Health Connect
     * @param classroomId The classroom ID (optional)
     * @param taskId The task ID (optional)
     * @param forcePost Whether to force posting regardless of change detection
     * @return ClientResponse indicating success or failure
     */
    suspend operator fun invoke(
        vitalSigns: VitalSigns,
        physicalId: String,
        studentId: String,
        classroomId: String,
        taskId: String,
        forcePost: Boolean = false
    ): ClientResponse<Unit> {
        return vitalsRestRepository.postVitalsIfChanged(
            vitalSigns = vitalSigns,
            physicalId = physicalId,
            studentId = studentId,
            classroomId = classroomId,
            taskId = taskId,
            forcePost = forcePost
        )
    }
    
    /**
     * Posts pre-activity vitals (always posts)
     */
    suspend fun postPreActivityVitals(
        vitalSigns: VitalSigns,
        physicalId: String,
        studentId: String,
        classroomId: String,
        taskId: String
    ): ClientResponse<Unit> {
        return vitalsRestRepository.postPreActivityVitals(
            vitalSigns = vitalSigns,
            physicalId = physicalId,
            studentId = studentId,
            classroomId = classroomId,
            taskId = taskId
        )
    }
    
    /**
     * Posts post-activity vitals (always posts)
     */
    suspend fun postPostActivityVitals(
        vitalSigns: VitalSigns,
        physicalId: String,
        studentId: String,
        classroomId: String,
        taskId: String
    ): ClientResponse<Unit> {
        return vitalsRestRepository.postPostActivityVitals(
            vitalSigns = vitalSigns,
            physicalId = physicalId,
            studentId = studentId,
            classroomId = classroomId,
            taskId = taskId
        )
    }
    
    /**
     * Resets the change detector for a new exercise session
     */
    fun resetForNewSession() {
        vitalsRestRepository.resetChangeDetector()
    }
}
