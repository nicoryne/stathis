package citu.edu.stathis.mobile.features.vitals.data.model

import java.time.LocalDateTime

/**
 * Data Transfer Object for REST API requests to post vital signs
 * This matches the backend API specification exactly
 */
data class VitalsRestRequestDto(
    val physicalId: String,
    val studentId: String,
    val classroomId: String,
    val taskId: String,
    val heartRate: Int,
    val oxygenSaturation: Int,
    val timestamp: LocalDateTime,
    val isPreActivity: Boolean = false,
    val isPostActivity: Boolean = false
) {
    companion object {
        /**
         * Creates a VitalsRestRequestDto from VitalSigns domain object
         */
        fun fromVitalSigns(
            vitalSigns: VitalSigns,
            physicalId: String,
            studentId: String,
            classroomId: String,
            taskId: String,
            isPreActivity: Boolean = false,
            isPostActivity: Boolean = false
        ): VitalsRestRequestDto {
            return VitalsRestRequestDto(
                physicalId = physicalId,
                studentId = studentId,
                classroomId = classroomId,
                taskId = taskId,
                heartRate = vitalSigns.heartRate,
                oxygenSaturation = vitalSigns.oxygenSaturation.toInt(),
                timestamp = vitalSigns.timestamp,
                isPreActivity = isPreActivity,
                isPostActivity = isPostActivity
            )
        }
    }
}
