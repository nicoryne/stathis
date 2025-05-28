package citu.edu.stathis.mobile.features.profile.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStudentProfileRequest(
    val school: String?,
    val course: String?,
    val yearLevel: Int?
)