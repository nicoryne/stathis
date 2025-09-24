package citu.edu.stathis.mobile.features.auth.data.models

import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles // Assuming your enum path
import kotlinx.serialization.Serializable // If you use Kotlinx Serialization for network DTOs

@Serializable
data class UserResponseDTO(
    val physicalId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val birthdate: String?,
    val profilePictureUrl: String?,
    val role: UserRoles,
    val school: String?,
    val course: String?,
    val yearLevel: Int?,  // Changed from String to Int to match backend
    val department: String?,  // Teacher-only field
    val positionTitle: String?,  // Teacher-only field
    val heightInMeters: Double?,  // Added missing field
    val weightInKg: Double?,  // Added missing field
    val emailVerified: Boolean  // Added missing field
)