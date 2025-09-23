package citu.edu.stathis.mobile.features.classroom.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO for classroom enrollment requests
 * 
 * This matches the exact Spring Boot DTO format expected by the backend
 * The server expects a JSON object with a code field
 */
data class EnrollmentRequest(
    @SerializedName("classroomCode")
    val classroomCode: String
) {
    // Helper method to directly use a code string as the request
    companion object {
        fun fromCode(code: String): EnrollmentRequest = EnrollmentRequest(classroomCode = code)
    }
}
