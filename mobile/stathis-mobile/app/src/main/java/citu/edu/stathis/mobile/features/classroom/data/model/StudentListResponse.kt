package citu.edu.stathis.mobile.features.classroom.data.model

data class StudentListResponse(
    val physicalId: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val profilePictureUrl: String?,
    val joinedAt: String?,
    val verified: Boolean
)


