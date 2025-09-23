package citu.edu.stathis.mobile.features.vitals.data.model

import java.time.LocalDateTime

/**
 * Data Transfer Object for sending vitals data via WebSocket
 * Maps to VitalSignsDTO on the backend
 */
data class VitalsWebSocketDTO(
    val physicalId: String,
    val studentId: String,
    val classroomId: String,
    val taskId: String,
    val heartRate: Int,
    val oxygenSaturation: Int,
    val timestamp: LocalDateTime,
    val isPreActivity: Boolean = false,
    val isPostActivity: Boolean = false
)
