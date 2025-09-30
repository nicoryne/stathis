package citu.edu.stathis.mobile.features.vitals.data.model

data class VitalsRequestDto(
    val studentId: String,
    val classroomId: String?,
    val taskId: String?,
    val heartRate: Int?,
    val oxygenSaturation: Int?, // Backend DTO uses Integer
    val timestamp: String, // ISO_DATE_TIME string
    val isPreActivity: Boolean?,
    val isPostActivity: Boolean?
    // Add other fields like bpSys, bpDia, temperature, respirationRate
    // Ensure names match backend (e.g., bp_sys, o2sat) or use @SerializedName with Gson
)