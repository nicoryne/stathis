package citu.edu.stathis.mobile.features.profile.domain.model

import java.time.LocalDateTime

data class UserProfile(
    val email: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val firstName: String,
    val lastName: String,
    val pictureUrl: String?,
    val userRole: String,
    val schoolAttending: String?,
    val yearLevel: Short?,
    val courseEnrolled: String?
) {
    val fullName: String
        get() = "$firstName $lastName"
}