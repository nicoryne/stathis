package citu.edu.stathis.mobile.features.vitals.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VitalsResponseDto(
    val physicalId: String, // from backend
    val studentId: String,
    val classroomId: String?,
    val taskId: String?,
    val heartRate: Int?,
    val oxygenSaturation: Int?,
    val timestamp: String,
    val isPreActivity: Boolean?,
    val isPostActivity: Boolean?,
    // Add other fields like bpSys, bpDia, temperature, respirationRate from backend
    val bpSys: Int?,
    val bpDia: Int?,
    val temperature: Float?,
    val respirationRate: Int?
    // Add deviceName if backend provides it
)