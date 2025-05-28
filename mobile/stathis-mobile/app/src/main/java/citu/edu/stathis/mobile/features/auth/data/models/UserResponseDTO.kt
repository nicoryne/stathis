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
    val yearLevel: String?,
    val department: String?,
    val positionTitle: String?
)