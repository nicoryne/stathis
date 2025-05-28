package citu.edu.stathis.mobile.features.profile.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserProfileRequest(
    val firstName: String,
    val lastName: String,
    val birthdate: String?,
    val profilePictureUrl: String?
)